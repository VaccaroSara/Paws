# Walkthrough - Advanced Filtering & User Types

I have implemented a professional filtering system that allows users to find exactly the puppy they are looking for based on multiple criteria.

## Changes Made

### 1. Advanced Filtering Logic
- **[HomeFragment.kt](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/java/com/example/paws/ui/screens/home/HomeFragment.kt)**:
    - **Interactive Filter Dialog**: Clicking the filter icon now opens a structured menu to filter by **Age**, **Gender**, **Animal Type**, and **User Type** (Private vs. Shelter).
    - **Real-time Updates**: The feed updates instantly as soon as a filter is selected.
    - **Clear Filters**: Added an option to reset all filters and see the full feed again.

### 2. User Type Tracking
- **[PuppyPost Model](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/java/com/example/paws/ui/screens/home/PostAdapter.kt)**: Updated to include `userType`.
- **[CreatePostFragment.kt](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/java/com/example/paws/ui/screens/home/CreatePostFragment.kt)**:
    - Now automatically fetches whether the creator is a "Private User" or an "Animal Shelter" during post creation/editing.
    - This data is saved in Firestore, enabling the new filtering capabilities.

### 3. UI Improvements
- **[fragment_home.xml](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/res/layout/fragment_home.xml)**: Added an ID to the filter icon to make it clickable and responsive.

## How to Verify
1. **Apply Filters**:
    - Go to the Home screen and click the **Filter** icon (next to the search bar).
    - Select **"Animal Type"** -> **"Dog"**.
    - Verify that only dogs are shown.
    - Add another filter: **"Gender"** -> **"Female"**.
    - Verify that only female dogs are now visible.
2. **User Type Filter**:
    - Select **"User Type"** -> **"Animal Shelter"**.
    - Verify that only posts from registered shelters are shown.
3. **Reset**:
    - Select **"Clear All Filters"** to return to the original global feed.

---
The Home feed is now a powerful search tool, helping users find their perfect companion more easily!
