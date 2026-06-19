# UI Fluidity, Empty States, and Account Deletion

The goal is to improve the user experience by eliminating flickering text during loading, providing clear feedback when no notifications are present, and adding a secure account deletion feature.

## Proposed Changes

### 1. UI Fluidity (Eliminating "Hi User")
To stop the temporary "hi user" or default profile text from showing while data is being fetched from Firestore:
- **[fragment_home.xml](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/res/layout/fragment_home.xml)**, **[fragment_favorites.xml](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/res/layout/fragment_favorites.xml)**, **[fragment_notifications.xml](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/res/layout/fragment_notifications.xml)**: Update the `tvWelcomeName` default text to be empty or just "hi, ".
- **[fragment_profile.xml](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/res/layout/fragment_profile.xml)**: Update default values for "Nome e Cognome", "Città", etc., to be empty or thin placeholders to prevent the visual "pop" once data arrives.

### 2. Notifications Empty State
- **[fragment_notifications.xml](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/res/layout/fragment_notifications.xml)**: Add a centered `TextView` with id `tvNoNotifications` that says "Non hai ancora nessuna notifica".
- **[NotificationsFragment.kt](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/java/com/example/paws/ui/screens/home/NotificationsFragment.kt)**: Show this text only when the notifications list is empty.

### 3. Account Deletion
- **[fragment_profile.xml](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/res/layout/fragment_profile.xml)**: Add a button `btnDeleteAccount` below the Logout button with a red style.
- **[ProfileFragment.kt](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/java/com/example/paws/ui/screens/home/ProfileFragment.kt)**:
    - Implement a confirmation dialog with a warning about data loss.
    - Delete user documents from Firestore (`users` and their `posts`).
    - Delete the user account from Firebase Auth.

---

## Verification Plan

### Manual Verification
1. **Flicker Test**: Navigate between tabs and verify that "hi user" or placeholder data no longer appears briefly.
2. **Empty Notifications**: Clear all notifications and verify the "Non hai ancora nessuna notifica" message appears.
3. **Account Deletion**: Use a test account, click "Elimina Account", confirm, and verify the app returns to the Splash/Login screen and the user is gone from Firebase.
