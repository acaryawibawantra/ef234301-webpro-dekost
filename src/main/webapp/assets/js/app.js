(function (global) {
    "use strict";

    var DEFAULT_TOAST_TIMEOUT_MS = 4500;
    var FOCUSABLE_SELECTOR =
        "button, [href], input:not([type=\"hidden\"]), select, textarea, [tabindex]:not([tabindex=\"-1\"])";

    var initializedRoots = new WeakSet();
    var modalTriggers = new WeakMap();
    var toastTimers = new WeakMap();
    var pendingConfirmForm = null;

    function resolveModal(root, modalOrId) {
        if (!root) {
            return null;
        }
        if (typeof modalOrId === "string") {
            return modalOrId ? root.getElementById(modalOrId) : null;
        }
        if (modalOrId && modalOrId.classList && modalOrId.classList.contains("modal")) {
            return modalOrId;
        }
        return null;
    }

    function updateBodyScrollLock(root) {
        if (!root || !root.body || !root.body.style) {
            return;
        }
        var hasOpenModal = root.querySelectorAll(".modal.is-open").length > 0;
        root.body.style.overflow = hasOpenModal ? "hidden" : "";
    }

    function focusModal(modal) {
        var firstFocusable = modal.querySelector(FOCUSABLE_SELECTOR);
        if (firstFocusable && typeof firstFocusable.focus === "function") {
            firstFocusable.focus();
            return;
        }
        var dialog = modal.querySelector(".modal__dialog");
        if (dialog && typeof dialog.focus === "function") {
            dialog.focus();
        }
    }

    function openModal(modalOrId, trigger, rootArg) {
        var root = rootArg || global.document;
        var modal = resolveModal(root, modalOrId);
        if (!modal) {
            return false;
        }
        if (trigger) {
            modalTriggers.set(modal, trigger);
        } else if (root.activeElement && root.activeElement !== root.body) {
            modalTriggers.set(modal, root.activeElement);
        }
        modal.classList.add("is-open");
        updateBodyScrollLock(root);
        focusModal(modal);
        return true;
    }

    function closeModal(modalOrId, rootArg) {
        var root = rootArg || global.document;
        if (!root) {
            return false;
        }
        var modal = null;
        if (modalOrId) {
            modal = resolveModal(root, modalOrId);
        } else {
            var openModals = root.querySelectorAll(".modal.is-open");
            if (openModals.length > 0) {
                modal = openModals[openModals.length - 1];
            }
        }
        if (!modal || !modal.classList.contains("is-open")) {
            return false;
        }

        modal.classList.remove("is-open");
        if (modal.id === "confirm-delete-modal") {
            pendingConfirmForm = null;
        }
        updateBodyScrollLock(root);

        var trigger = modalTriggers.get(modal);
        modalTriggers.delete(modal);
        if (trigger && trigger.isConnected && typeof trigger.focus === "function") {
            trigger.focus();
        }
        return true;
    }

    function openConfirmDelete(trigger, root) {
        var modalId = trigger.getAttribute("data-confirm-open") || "confirm-delete-modal";
        var modal = resolveModal(root, modalId);
        if (!modal) {
            return false;
        }

        pendingConfirmForm = null;

        var form = modal.querySelector("form");
        var deleteUrl = trigger.getAttribute("data-delete-url");
        if (form && deleteUrl) {
            form.setAttribute("action", deleteUrl);
        }

        var deleteId = trigger.getAttribute("data-delete-id");
        if (form && deleteId !== null) {
            var idInput = form.querySelector('input[name="id"]');
            if (idInput) {
                idInput.value = deleteId;
            }
        }

        var titleEl = modal.querySelector("[data-confirm-title]");
        if (titleEl) {
            titleEl.textContent = trigger.getAttribute("data-delete-title") || "Konfirmasi hapus";
        }

        var msgEl = modal.querySelector("[data-confirm-message]");
        if (msgEl) {
            msgEl.textContent =
                trigger.getAttribute("data-delete-message") ||
                "Data yang dihapus tidak dapat dikembalikan.";
        }

        return openModal(modal, trigger, root);
    }

    function dismissToast(toast) {
        if (!toast) {
            return;
        }
        var timerId = toastTimers.get(toast);
        if (timerId !== undefined) {
            clearTimeout(timerId);
            toastTimers.delete(toast);
        }
        toast.remove();
    }

    function resolveToastTimeout(toast, explicitTimeout) {
        if (typeof explicitTimeout === "number") {
            return explicitTimeout;
        }
        var attr = toast.getAttribute("data-toast-timeout");
        if (attr === null && toast.parentElement) {
            attr = toast.parentElement.getAttribute("data-toast-timeout");
        }
        if (attr !== null && attr !== "") {
            var parsed = Number(attr);
            if (!Number.isNaN(parsed) && parsed >= 0) {
                return parsed;
            }
        }
        return DEFAULT_TOAST_TIMEOUT_MS;
    }

    function scheduleToast(toast, explicitTimeout) {
        if (!toast || toastTimers.has(toast)) {
            return;
        }
        var timeoutMs = resolveToastTimeout(toast, explicitTimeout);
        if (timeoutMs <= 0) {
            return;
        }
        var timerId = setTimeout(function () {
            toastTimers.delete(toast);
            if (toast.isConnected) {
                toast.remove();
            }
        }, timeoutMs);
        toastTimers.set(toast, timerId);
    }

    function showToast(message, type, timeoutMs, rootArg) {
        var root = rootArg || global.document;
        if (!root || !root.body || !message) {
            return null;
        }

        var stack = root.querySelector(".toast-stack");
        if (!stack) {
            stack = root.createElement("div");
            stack.className = "toast-stack";
            stack.setAttribute("aria-live", "polite");
            root.body.appendChild(stack);
        }

        var toast = root.createElement("p");
        toast.className = "toast";
        if (type === "error") {
            toast.classList.add("toast--error");
        } else if (type === "warning") {
            toast.classList.add("toast--warning");
        }
        toast.setAttribute("role", "status");
        toast.textContent = String(message);
        stack.appendChild(toast);

        scheduleToast(toast, timeoutMs);
        return toast;
    }

    function handleDocumentClick(root, event) {
        var target = event.target;
        if (!target || typeof target.closest !== "function") {
            return;
        }

        var confirmTrigger = target.closest("[data-confirm-open], [data-delete-url]");
        if (confirmTrigger) {
            event.preventDefault();
            openConfirmDelete(confirmTrigger, root);
            return;
        }

        var openTrigger = target.closest("[data-modal-open]");
        if (openTrigger) {
            event.preventDefault();
            openModal(openTrigger.getAttribute("data-modal-open"), openTrigger, root);
            return;
        }

        var closeTrigger = target.closest("[data-modal-close], .modal__close");
        if (closeTrigger) {
            event.preventDefault();
            closeModal(closeTrigger.closest(".modal"), root);
            return;
        }

        if (target.classList && target.classList.contains("modal") && target.classList.contains("is-open")) {
            closeModal(target, root);
            return;
        }

        var confirmSubmit = target.closest("#confirm-delete-modal [data-confirm-submit]");
        if (confirmSubmit && pendingConfirmForm) {
            event.preventDefault();
            var formToSubmit = pendingConfirmForm;
            pendingConfirmForm = null;
            formToSubmit.dataset.confirmed = "true";
            closeModal("confirm-delete-modal", root);
            if (typeof formToSubmit.requestSubmit === "function") {
                formToSubmit.requestSubmit();
            } else {
                formToSubmit.submit();
            }
            return;
        }

        var toastDismiss = target.closest("[data-toast-dismiss]");
        if (toastDismiss) {
            dismissToast(toastDismiss.closest(".toast"));
            return;
        }

        var toast = target.closest(".toast");
        if (toast) {
            dismissToast(toast);
        }
    }

    function handleDocumentKeydown(root, event) {
        if (event.key === "Escape") {
            closeModal(null, root);
        }
    }

    function handleDocumentSubmit(root, event) {
        var form = event.target;
        if (!form || typeof form.getAttribute !== "function") {
            return;
        }
        var confirmMessage = form.getAttribute("data-confirm");
        if (!confirmMessage || form.dataset.confirmed === "true") {
            return;
        }

        var modal = resolveModal(root, "confirm-delete-modal");
        if (!modal) {
            return;
        }

        event.preventDefault();
        pendingConfirmForm = form;
        var titleEl = modal.querySelector("[data-confirm-title]");
        if (titleEl) {
            titleEl.textContent = form.getAttribute("data-confirm-title") || "Konfirmasi";
        }
        var msgEl = modal.querySelector("[data-confirm-message]");
        if (msgEl) {
            msgEl.textContent = confirmMessage;
        }
        openModal(modal, null, root);
    }

    function init(rootArg) {
        var root = rootArg || global.document;
        if (!root || initializedRoots.has(root)) {
            return;
        }
        initializedRoots.add(root);

        root.addEventListener("click", function (event) {
            handleDocumentClick(root, event);
        });
        root.addEventListener("keydown", function (event) {
            handleDocumentKeydown(root, event);
        });
        root.addEventListener("submit", function (event) {
            handleDocumentSubmit(root, event);
        });

        var existingToasts = root.querySelectorAll(".toast-stack .toast");
        for (var i = 0; i < existingToasts.length; i++) {
            scheduleToast(existingToasts[i]);
        }
        updateBodyScrollLock(root);
    }

    global.DeKostUI = {
        openModal: function (modalOrId, trigger) {
            return openModal(modalOrId, trigger, global.document);
        },
        closeModal: function (modalOrId) {
            return closeModal(modalOrId, global.document);
        },
        showToast: function (message, type, timeoutMs) {
            return showToast(message, type, timeoutMs, global.document);
        },
        init: init
    };

    if (global.document) {
        if (global.document.readyState === "loading") {
            global.document.addEventListener("DOMContentLoaded", function () {
                init(global.document);
            });
        } else {
            init(global.document);
        }
    }
})(window);
