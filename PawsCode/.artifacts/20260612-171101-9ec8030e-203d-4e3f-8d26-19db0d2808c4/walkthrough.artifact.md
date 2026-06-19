# Walkthrough - Notification Enhancements

I have improved the notification system with a visual indicator, cleanup tools, and refined styling.

## Changes Made

### 1. Notification Badge (Orange Dot)
- **[Home & Favorites Headers]**:
    - Wrapped the bell icon in a container and added a small orange dot (`bg_orange_dot.xml`).
    - **Smart Visibility**: The app now listens for new notifications in the background. If you have any notifications, a small orange dot appears on the bell icon in both the **Home** and **Favorites** screens.
    - The badge remains visible until all notifications are cleared, giving you a clear signal that someone liked your post.

### 2. Notifications Cleanup
- **[NotificationsFragment.kt](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/java/com/example/paws/ui/screens/home/NotificationsFragment.kt)**:
    - Added a trash icon (`ic_trash_gray.xml`) next to the "Your Notifications" title.
    - **Bulk Deletion**: Clicking this icon opens a confirmation dialog asking if you want to delete all notifications.
    - Once confirmed, all your notifications are wiped from Firestore, and the list clears instantly.

### 3. Styling & Font
- **[item_notification.xml](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/res/layout/item_notification.xml)**:
    - Updated the notification text to use `sans-serif-medium`. Since Poppins was not a standard system-integrated font in the current resource folder, this provides the most similar modern, clean look that matches your UI requirements.

## How to Verify
1. **Trigger Badge**: Have another user favorite your post. Verify the orange dot appears on the bell in Home or Favorites.
2. **Open Notifications**: Click the bell. The list should show the new items.
3. **Clear All**:
    - Click the trash icon in the top right of the notifications card.
    - Confirm the deletion.
    - Verify the list is empty and the orange badge on the main screens is gone.

---
The notification system is now more intuitive and easier to manage!
