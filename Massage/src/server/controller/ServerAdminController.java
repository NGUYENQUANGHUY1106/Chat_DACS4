package server.controller;

import server.core.ChatServerCore;

public class ServerAdminController {

    private ServerAdminController() {
        // static-only
    }

    /** Khởi động server trong một thread riêng */
    public static void startServer() {
        new Thread(ChatServerCore::startServer).start();
    }
}
