import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import vm from "node:vm";

const APP_JS_PATH = path.resolve(process.cwd(), "src/main/webapp/assets/js/app.js");

function toCamelCase(name) {
    return name.replace(/-([a-z])/g, (_, ch) => ch.toUpperCase());
}

function toKebabCase(name) {
    return name.replace(/[A-Z]/g, (ch) => `-${ch.toLowerCase()}`);
}

function matchesSimpleSelector(el, selector) {
    const trimmed = selector.trim();
    if (!trimmed) return false;

    const parts = trimmed.split(/\s+/);
    if (parts.length > 1) {
        const last = parts[parts.length - 1];
        if (!matchesCompound(el, last)) return false;
        let current = el.parentElement;
        for (let i = parts.length - 2; i >= 0; i--) {
            while (current && !matchesCompound(current, parts[i])) {
                current = current.parentElement;
            }
            if (!current) return false;
            current = current.parentElement;
        }
        return true;
    }

    return matchesCompound(el, trimmed);
}

function matchesCompound(el, compound) {
    const attrRegex = /\[([^\]=]+)(?:="([^"]*)")?\]/g;
    let remaining = compound;
    let match;
    while ((match = attrRegex.exec(compound)) !== null) {
        const attrName = match[1];
        const expectedVal = match[2];
        if (!el.hasAttribute(attrName)) return false;
        if (expectedVal !== undefined && el.getAttribute(attrName) !== expectedVal) {
            return false;
        }
        remaining = remaining.replace(match[0], "");
    }

    const notRegex = /:not\(([^)]+)\)/g;
    while ((match = notRegex.exec(remaining)) !== null) {
        if (matchesCompound(el, match[1])) return false;
        remaining = remaining.replace(match[0], "");
    }

    const tokens = remaining.match(/(#[^.#]+|\.[^.#]+|^[a-zA-Z0-9_-]+)/g) || [];
    for (const token of tokens) {
        if (token.startsWith("#")) {
            if (el.id !== token.slice(1)) return false;
        } else if (token.startsWith(".")) {
            if (!el.classList.contains(token.slice(1))) return false;
        } else if (el.tagName.toLowerCase() !== token.toLowerCase()) {
            return false;
        }
    }
    return true;
}

function matchesSelectorList(el, selectorList) {
    return selectorList.split(",").some((s) => matchesSimpleSelector(el, s));
}

function createDomEnvironment() {
    const listeners = new Map();

    class FakeElement {
        constructor(tagName, ownerDocument, attrs = {}) {
            this.tagName = tagName.toUpperCase();
            this.ownerDocument = ownerDocument;
            this.children = [];
            this.parentElement = null;
            this.style = {};
            this._attrs = new Map();
            this._classes = new Set();
            this._textContent = "";
            this._innerHTML = "";
            this.value = "";
            this.submitted = false;
            this.listeners = new Map();

            const self = this;
            this.classList = {
                add(...names) {
                    names.forEach((n) => self._classes.add(n));
                },
                remove(...names) {
                    names.forEach((n) => self._classes.delete(n));
                },
                contains(name) {
                    return self._classes.has(name);
                }
            };

            this.dataset = new Proxy(
                {},
                {
                    get(_, prop) {
                        return self.getAttribute(`data-${toKebabCase(String(prop))}`) ?? undefined;
                    },
                    set(_, prop, val) {
                        self.setAttribute(`data-${toKebabCase(String(prop))}`, String(val));
                        return true;
                    }
                }
            );

            for (const [k, v] of Object.entries(attrs)) {
                this.setAttribute(k, v);
            }
        }

        get id() {
            return this.getAttribute("id") || "";
        }

        set id(val) {
            this.setAttribute("id", val);
        }

        get className() {
            return Array.from(this._classes).join(" ");
        }

        set className(val) {
            this._classes = new Set(String(val).split(/\s+/).filter(Boolean));
        }

        get action() {
            return this.getAttribute("action") || "";
        }

        set action(val) {
            this.setAttribute("action", val);
        }

        get textContent() {
            return this._textContent;
        }

        set textContent(val) {
            this._textContent = String(val);
        }

        get innerHTML() {
            return this._innerHTML;
        }

        set innerHTML(val) {
            this._innerHTML = String(val);
        }

        get isConnected() {
            let cur = this;
            while (cur) {
                if (cur === this.ownerDocument.documentElement) return true;
                cur = cur.parentElement;
            }
            return false;
        }

        setAttribute(name, value) {
            if (name === "class") {
                this.className = value;
                return;
            }
            this._attrs.set(name, String(value));
        }

        getAttribute(name) {
            if (name === "class") return this.className;
            return this._attrs.has(name) ? this._attrs.get(name) : null;
        }

        hasAttribute(name) {
            if (name === "class") return this._classes.size > 0;
            return this._attrs.has(name);
        }

        appendChild(child) {
            child.parentElement = this;
            this.children.push(child);
            return child;
        }

        remove() {
            if (!this.parentElement) return;
            this.parentElement.children = this.parentElement.children.filter((c) => c !== this);
            this.parentElement = null;
        }

        focus() {
            this.ownerDocument.activeElement = this;
        }

        submit() {
            this.submitted = true;
        }

        requestSubmit() {
            const event = {
                type: "submit",
                target: this,
                defaultPrevented: false,
                preventDefault() {
                    this.defaultPrevented = true;
                }
            };
            this.ownerDocument.dispatchEvent(event);
            if (!event.defaultPrevented) {
                this.submitted = true;
            }
        }

        closest(selector) {
            let cur = this;
            while (cur) {
                if (matchesSelectorList(cur, selector)) return cur;
                cur = cur.parentElement;
            }
            return null;
        }

        querySelector(selector) {
            return this.querySelectorAll(selector)[0] || null;
        }

        querySelectorAll(selector) {
            const results = [];
            const visit = (node) => {
                for (const child of node.children) {
                    if (matchesSelectorList(child, selector)) {
                        results.push(child);
                    }
                    visit(child);
                }
            };
            visit(this);
            return results;
        }

        addEventListener(type, fn) {
            if (!this.listeners.has(type)) this.listeners.set(type, []);
            this.listeners.get(type).push(fn);
        }
    }

    const doc = {
        readyState: "complete",
        activeElement: null,
        createElement(tag) {
            return new FakeElement(tag, doc);
        },
        addEventListener(type, fn) {
            if (!listeners.has(type)) listeners.set(type, []);
            listeners.get(type).push(fn);
        },
        dispatchEvent(event) {
            const handlers = listeners.get(event.type) || [];
            for (const fn of handlers) {
                fn(event);
            }
        },
        querySelector(selector) {
            return doc.documentElement.querySelector(selector);
        },
        querySelectorAll(selector) {
            return doc.documentElement.querySelectorAll(selector);
        },
        getElementById(id) {
            return doc.querySelector(`#${id}`);
        }
    };

    doc.documentElement = new FakeElement("html", doc);
    doc.body = new FakeElement("body", doc);
    doc.documentElement.appendChild(doc.body);

    const win = {
        document: doc,
        setTimeout,
        clearTimeout
    };

    return {
        window: win,
        document: doc,
        el(tag, attrs = {}, children = []) {
            const node = new FakeElement(tag, doc, attrs);
            children.forEach((c) => node.appendChild(c));
            return node;
        },
        click(target) {
            doc.dispatchEvent({
                type: "click",
                target,
                defaultPrevented: false,
                preventDefault() {
                    this.defaultPrevented = true;
                }
            });
        },
        keydown(key) {
            doc.dispatchEvent({
                type: "keydown",
                key,
                defaultPrevented: false,
                preventDefault() {
                    this.defaultPrevented = true;
                }
            });
        }
    };
}

function loadApp(env) {
    const code = fs.readFileSync(APP_JS_PATH, "utf8");
    const context = vm.createContext({
        window: env.window,
        document: env.document,
        setTimeout,
        clearTimeout,
        console
    });
    vm.runInContext(code, context);
    return env.window.DeKostUI;
}

test("openModal and closeModal toggle .is-open via trigger, close buttons, backdrop, and Escape", () => {
    const env = createDomEnvironment();
    const trigger = env.el("button", { type: "button", "data-modal-open": "kamar-modal" });
    const closeBtn = env.el("button", { type: "button", class: "modal__close" });
    const cancelBtn = env.el("button", { type: "button", "data-modal-close": "" });
    const input = env.el("input", { type: "text" });
    const dialog = env.el("div", { class: "modal__dialog" }, [closeBtn, input, cancelBtn]);
    const modal = env.el("div", { id: "kamar-modal", class: "modal" }, [dialog]);

    env.document.body.appendChild(trigger);
    env.document.body.appendChild(modal);

    const ui = loadApp(env);
    assert.ok(ui, "window.DeKostUI must be defined");

    trigger.focus();
    env.click(trigger);
    assert.equal(modal.classList.contains("is-open"), true);
    assert.equal(env.document.body.style.overflow, "hidden");
    assert.equal(env.document.activeElement, closeBtn);

    env.click(closeBtn);
    assert.equal(modal.classList.contains("is-open"), false);
    assert.equal(env.document.body.style.overflow, "");
    assert.equal(env.document.activeElement, trigger);

    env.click(trigger);
    assert.equal(modal.classList.contains("is-open"), true);
    env.click(cancelBtn);
    assert.equal(modal.classList.contains("is-open"), false);

    env.click(trigger);
    assert.equal(modal.classList.contains("is-open"), true);
    env.click(modal);
    assert.equal(modal.classList.contains("is-open"), false);

    env.click(trigger);
    assert.equal(modal.classList.contains("is-open"), true);
    env.keydown("Escape");
    assert.equal(modal.classList.contains("is-open"), false);
});

test("pre-opened .modal.is-open closes cleanly via Escape and backdrop without trigger", () => {
    const env = createDomEnvironment();
    const modal = env.el("div", { id: "edit-modal", class: "modal is-open" }, [
        env.el("div", { class: "modal__dialog" })
    ]);
    env.document.body.appendChild(modal);

    loadApp(env);
    env.keydown("Escape");
    assert.equal(modal.classList.contains("is-open"), false);
    assert.equal(env.document.body.style.overflow, "");
});

test("missing modal target on [data-modal-open] and Escape without open modal are safe no-ops", () => {
    const env = createDomEnvironment();
    const trigger = env.el("button", { type: "button", "data-modal-open": "nonexistent" });
    env.document.body.appendChild(trigger);

    const ui = loadApp(env);
    assert.doesNotThrow(() => {
        env.click(trigger);
        env.keydown("Escape");
        assert.equal(ui.openModal("nonexistent"), false);
        assert.equal(ui.closeModal(), false);
    });
});

test("delete confirmation trigger populates form action, id, title, and XSS-safe textContent message", () => {
    const env = createDomEnvironment();
    const xssPayload = 'Hapus kamar <script>alert(1)</script>?';
    const deleteBtn = env.el("button", {
        type: "button",
        "data-confirm-open": "confirm-delete-modal",
        "data-delete-url": "/admin/kamar/delete",
        "data-delete-id": "42",
        "data-delete-title": "Hapus Kamar 101",
        "data-delete-message": xssPayload
    });

    const titleEl = env.el("h2", { class: "modal__title", "data-confirm-title": "" });
    const msgEl = env.el("p", { "data-confirm-message": "" });
    const idInput = env.el("input", { type: "hidden", name: "id", value: "" });
    const form = env.el("form", { method: "post", action: "/admin/dashboard" }, [idInput, msgEl]);
    const modal = env.el("div", { id: "confirm-delete-modal", class: "modal" }, [
        env.el("div", { class: "modal__dialog" }, [titleEl, form])
    ]);

    env.document.body.appendChild(deleteBtn);
    env.document.body.appendChild(modal);

    loadApp(env);
    env.click(deleteBtn);

    assert.equal(modal.classList.contains("is-open"), true);
    assert.equal(form.getAttribute("action"), "/admin/kamar/delete");
    assert.equal(idInput.value, "42");
    assert.equal(titleEl.textContent, "Hapus Kamar 101");
    assert.equal(msgEl.textContent, xssPayload);
    assert.equal(msgEl.innerHTML, "");
});

test("toast auto-dismisses after timeout, dismisses on click, and respects data-toast-timeout=0", async () => {
    const env = createDomEnvironment();
    const persistentToast = env.el("p", { class: "toast", "data-toast-timeout": "0" });
    persistentToast.textContent = "Tetap tampil";
    const stack = env.el("div", { class: "toast-stack", "data-toast-timeout": "40" }, [
        persistentToast
    ]);
    env.document.body.appendChild(stack);

    const ui = loadApp(env);
    const autoToast = ui.showToast("Tersimpan", "success", 30);
    assert.ok(autoToast);
    assert.equal(autoToast.textContent, "Tersimpan");

    const errorToast = ui.showToast("Gagal", "error", 100);
    assert.equal(errorToast.classList.contains("toast--error"), true);
    env.click(errorToast);
    assert.equal(errorToast.isConnected, false);

    await new Promise((r) => setTimeout(r, 70));
    assert.equal(autoToast.isConnected, false);
    assert.equal(persistentToast.isConnected, true);
});
