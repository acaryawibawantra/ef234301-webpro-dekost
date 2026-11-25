import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import vm from "node:vm";

const APP_JS_PATH = path.resolve(process.cwd(), "src/main/webapp/assets/js/app.js");

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
    let remaining = compound;
    const notRegex = /:not\(([^)]+)\)/g;
    let match;
    while ((match = notRegex.exec(compound)) !== null) {
        if (matchesCompound(el, match[1])) return false;
        remaining = remaining.replace(match[0], "");
    }

    const attrRegex = /\[([^\]=]+)(?:="([^"]*)")?\]/g;
    let withoutAttrs = remaining;
    while ((match = attrRegex.exec(remaining)) !== null) {
        const attrName = match[1];
        const expectedVal = match[2];
        if (!el.hasAttribute(attrName)) return false;
        if (expectedVal !== undefined && el.getAttribute(attrName) !== expectedVal) {
            return false;
        }
        withoutAttrs = withoutAttrs.replace(match[0], "");
    }

    const tokens = withoutAttrs.match(/(#[^.#]+|\.[^.#]+|^[a-zA-Z0-9_-]+)/g) || [];
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
                    },
                    deleteProperty(_, prop) {
                        self._attrs.delete(`data-${toKebabCase(String(prop))}`);
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
            if (name === "value") {
                this.value = String(value);
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
        },
        submit(form) {
            const event = {
                type: "submit",
                target: form,
                defaultPrevented: false,
                preventDefault() {
                    this.defaultPrevented = true;
                }
            };
            doc.dispatchEvent(event);
            if (!event.defaultPrevented) {
                form.submitted = true;
            }
            return event;
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
    const hiddenInput = env.el("input", { type: "hidden", name: "id" });
    const textInput = env.el("input", { type: "text", name: "nomor" });
    const closeBtn = env.el("button", { type: "button", class: "modal__close" });
    const cancelBtn = env.el("button", { type: "button", "data-modal-close": "" });
    const dialog = env.el("div", { class: "modal__dialog" }, [hiddenInput, textInput, closeBtn, cancelBtn]);
    const modal = env.el("div", { id: "kamar-modal", class: "modal" }, [dialog]);

    env.document.body.appendChild(trigger);
    env.document.body.appendChild(modal);

    const ui = loadApp(env);
    assert.ok(ui, "window.DeKostUI must be defined");

    trigger.focus();
    env.click(trigger);
    assert.equal(modal.classList.contains("is-open"), true);
    assert.equal(env.document.body.style.overflow, "hidden");
    assert.equal(env.document.activeElement, textInput);

    env.click(closeBtn);
    assert.equal(modal.classList.contains("is-open"), false);
    assert.equal(env.document.body.style.overflow, "");
    assert.equal(env.document.activeElement, trigger);

    env.click(trigger);
    env.click(cancelBtn);
    assert.equal(modal.classList.contains("is-open"), false);

    env.click(trigger);
    env.click(modal);
    assert.equal(modal.classList.contains("is-open"), false);

    env.click(trigger);
    env.keydown("Escape");
    assert.equal(modal.classList.contains("is-open"), false);
});

test("stacked modals keep body scroll locked until the last modal closes", () => {
    const env = createDomEnvironment();
    const m1 = env.el("div", { id: "m1", class: "modal" }, [env.el("div", { class: "modal__dialog" })]);
    const m2 = env.el("div", { id: "m2", class: "modal" }, [env.el("div", { class: "modal__dialog" })]);
    env.document.body.appendChild(m1);
    env.document.body.appendChild(m2);

    const ui = loadApp(env);
    ui.openModal("m1");
    ui.openModal("m2");
    assert.equal(env.document.body.style.overflow, "hidden");

    env.keydown("Escape");
    assert.equal(m2.classList.contains("is-open"), false);
    assert.equal(m1.classList.contains("is-open"), true);
    assert.equal(env.document.body.style.overflow, "hidden");

    env.keydown("Escape");
    assert.equal(m1.classList.contains("is-open"), false);
    assert.equal(env.document.body.style.overflow, "");
});

test("pre-opened .modal.is-open locks body scroll on load and closes via Escape, close buttons, and backdrop", () => {
    const env = createDomEnvironment();
    const closeBtn = env.el("button", { type: "button", class: "modal__close" });
    const cancelBtn = env.el("button", { type: "button", "data-modal-close": "" });
    const modal = env.el("div", { id: "edit-modal", class: "modal is-open" }, [
        env.el("div", { class: "modal__dialog" }, [closeBtn, cancelBtn])
    ]);
    env.document.body.appendChild(modal);

    loadApp(env);
    assert.equal(env.document.body.style.overflow, "hidden");

    env.keydown("Escape");
    assert.equal(modal.classList.contains("is-open"), false);
    assert.equal(env.document.body.style.overflow, "");

    modal.classList.add("is-open");
    env.click(closeBtn);
    assert.equal(modal.classList.contains("is-open"), false);

    modal.classList.add("is-open");
    env.click(cancelBtn);
    assert.equal(modal.classList.contains("is-open"), false);

    modal.classList.add("is-open");
    env.click(modal);
    assert.equal(modal.classList.contains("is-open"), false);
});

test("missing modal target on [data-modal-open], [data-confirm-open], and Escape without open modal are safe no-ops", () => {
    const env = createDomEnvironment();
    const trigger = env.el("button", { type: "button", "data-modal-open": "nonexistent" });
    const confirmTrigger = env.el("button", {
        type: "button",
        "data-confirm-open": "confirm-delete-modal",
        "data-delete-url": "/admin/kamar/delete",
        "data-delete-id": "1"
    });
    env.document.body.appendChild(trigger);
    env.document.body.appendChild(confirmTrigger);

    const ui = loadApp(env);
    assert.doesNotThrow(() => {
        env.click(trigger);
        env.click(confirmTrigger);
        env.keydown("Escape");
        assert.equal(ui.openModal("nonexistent"), false);
        assert.equal(ui.closeModal(), false);
    });
});

test("delete confirmation resets stale id and action across consecutive triggers and escapes XSS/quotes", () => {
    const env = createDomEnvironment();
    const xssPayload = 'Hapus "Kamar" \'101\' <script>alert(1)</script>?';
    const firstBtn = env.el("button", {
        type: "button",
        "data-confirm-open": "confirm-delete-modal",
        "data-delete-url": "/admin/kamar/delete",
        "data-delete-id": "42",
        "data-delete-title": 'Hapus "Kamar 101"',
        "data-delete-message": xssPayload
    });
    const secondBtn = env.el("button", {
        type: "button",
        "data-confirm-open": "confirm-delete-modal"
    });

    const titleEl = env.el("h2", { class: "modal__title", "data-confirm-title": "" });
    const msgEl = env.el("p", { "data-confirm-message": "" });
    const idInput = env.el("input", { type: "hidden", name: "id", value: "" });
    const cancelBtn = env.el("button", { type: "button", "data-modal-close": "" });
    const form = env.el("form", { method: "post", action: "/admin/dashboard" }, [idInput, msgEl, cancelBtn]);
    const modal = env.el("div", { id: "confirm-delete-modal", class: "modal" }, [
        env.el("div", { class: "modal__dialog" }, [titleEl, form])
    ]);

    env.document.body.appendChild(firstBtn);
    env.document.body.appendChild(secondBtn);
    env.document.body.appendChild(modal);

    loadApp(env);
    env.click(firstBtn);

    assert.equal(modal.classList.contains("is-open"), true);
    assert.equal(form.getAttribute("action"), "/admin/kamar/delete");
    assert.equal(idInput.value, "42");
    assert.equal(titleEl.textContent, 'Hapus "Kamar 101"');
    assert.equal(msgEl.textContent, xssPayload);
    assert.equal(msgEl.innerHTML, "");

    env.click(cancelBtn);
    assert.equal(idInput.value, "");
    assert.equal(form.getAttribute("action"), "/admin/dashboard");

    env.click(secondBtn);
    assert.equal(idInput.value, "");
    assert.equal(form.getAttribute("action"), "/admin/dashboard");
    assert.equal(titleEl.textContent, "Konfirmasi hapus");
});

test("form[data-confirm] intercepts submit, submits on confirm, and clears pending state on cancel", () => {
    const env = createDomEnvironment();
    const targetForm = env.el("form", {
        method: "post",
        action: "/admin/penghuni/checkout",
        "data-confirm": "Check-out penghuni ini?",
        "data-confirm-title": "Konfirmasi check-out"
    });

    const titleEl = env.el("h2", { class: "modal__title", "data-confirm-title": "" });
    const msgEl = env.el("p", { "data-confirm-message": "" });
    const cancelBtn = env.el("button", { type: "button", "data-modal-close": "" });
    const confirmSubmitBtn = env.el("button", { type: "submit", "data-confirm-submit": "" });
    const modalForm = env.el("form", { method: "post", action: "/admin/dashboard" }, [
        msgEl,
        cancelBtn,
        confirmSubmitBtn
    ]);
    const modal = env.el("div", { id: "confirm-delete-modal", class: "modal" }, [
        env.el("div", { class: "modal__dialog" }, [titleEl, modalForm])
    ]);

    env.document.body.appendChild(targetForm);
    env.document.body.appendChild(modal);

    loadApp(env);

    env.submit(targetForm);
    assert.equal(targetForm.submitted, false);
    assert.equal(modal.classList.contains("is-open"), true);
    assert.equal(titleEl.textContent, "Konfirmasi check-out");
    assert.equal(msgEl.textContent, "Check-out penghuni ini?");

    env.click(cancelBtn);
    assert.equal(modal.classList.contains("is-open"), false);
    assert.equal(targetForm.submitted, false);

    env.submit(targetForm);
    assert.equal(modal.classList.contains("is-open"), true);
    env.click(confirmSubmitBtn);
    assert.equal(modal.classList.contains("is-open"), false);
    assert.equal(targetForm.submitted, true);
});

test("URL hash opens the matching modal on load and clears the hash afterwards", () => {
    const env = createDomEnvironment();
    env.document.location = { hash: "#kamar-create-modal", pathname: "/admin/kamar", search: "?status=TERSEDIA" };
    let replacedUrl = null;
    env.window.history = {
        replaceState(_state, _title, url) {
            replacedUrl = url;
        }
    };

    const modal = env.el("div", { id: "kamar-create-modal", class: "modal" }, [
        env.el("div", { class: "modal__dialog" }, [env.el("input", { type: "text", name: "nomorKamar" })])
    ]);
    const unrelated = env.el("div", { id: "other-modal", class: "modal" }, [
        env.el("div", { class: "modal__dialog" })
    ]);
    env.document.body.appendChild(modal);
    env.document.body.appendChild(unrelated);

    loadApp(env);

    assert.equal(modal.classList.contains("is-open"), true);
    assert.equal(unrelated.classList.contains("is-open"), false);
    assert.equal(replacedUrl, "/admin/kamar?status=TERSEDIA");
});

test("toast auto-dismisses server & dynamic toasts, cancels timer on click, creates stack when missing, and respects timeout=0", async () => {
    const env = createDomEnvironment();
    const ui = loadApp(env);

    const createdToast = ui.showToast("Stack dibuat otomatis", "warning", 25);
    assert.ok(createdToast);
    assert.equal(createdToast.classList.contains("toast--warning"), true);
    assert.ok(env.document.querySelector(".toast-stack"));

    const persistentToast = env.el("p", { class: "toast", "data-toast-timeout": "0" });
    persistentToast.textContent = "Tetap tampil";
    const stack = env.document.querySelector(".toast-stack");
    stack.appendChild(persistentToast);

    const manualToast = ui.showToast("Gagal", "error", 20);
    assert.equal(manualToast.classList.contains("toast--error"), true);
    env.click(manualToast);
    assert.equal(manualToast.isConnected, false);

    await new Promise((r) => setTimeout(r, 70));
    assert.equal(createdToast.isConnected, false);
    assert.equal(persistentToast.isConnected, true);
});
