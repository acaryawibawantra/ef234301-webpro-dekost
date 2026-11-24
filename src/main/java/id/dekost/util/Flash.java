package id.dekost.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;

public final class Flash {

    public static final String REQUEST_ATTRIBUTE = "flash";
    private static final String SESSION_ATTRIBUTE = Flash.class.getName() + ".message";

    private Flash() {
    }

    public static void success(HttpServletRequest request, String message) {
        put(request, new Message("success", message));
    }

    public static void error(HttpServletRequest request, String message) {
        put(request, new Message("error", message));
    }

    public static Message consume(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Message message = (Message) session.getAttribute(SESSION_ATTRIBUTE);
        session.removeAttribute(SESSION_ATTRIBUTE);
        if (message != null) {
            request.setAttribute(REQUEST_ATTRIBUTE, message);
        }
        return message;
    }

    private static void put(HttpServletRequest request, Message message) {
        request.getSession().setAttribute(SESSION_ATTRIBUTE, message);
    }

    public static final class Message implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String type;
        private final String text;

        private Message(String type, String text) {
            this.type = type;
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }
}
