package application.model;

public final class UserSession {

    private static int currentUserId = -1;
    private static String currentUsername;

    private UserSession() {
    }

    public static void setCurrentUser(int userId, String username) {
        currentUserId = userId;
        currentUsername = username;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static boolean isLoggedIn() {
        return currentUserId != -1;
    }

    public static void clear() {
        currentUserId = -1;
        currentUsername = null;
    }
}