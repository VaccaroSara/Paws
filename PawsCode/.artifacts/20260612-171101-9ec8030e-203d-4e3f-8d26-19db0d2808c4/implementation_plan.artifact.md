# Implement Advanced Filtering and User Types

The goal is to implement a robust filtering system for the Home feed that allows users to filter by Age, Gender, Animal Type, and the type of user who posted (Private vs. Shelter).

## User Review Required
- **User Data**: To filter by "Private" or "Shelter", we need this info in the `posts` collection. I will update the post creation logic to include the `userType` of the owner at the time of posting.
- **Filter Persistence**: Filters will be applied locally to the current feed for immediate response, or by re-querying Firestore if needed for larger datasets.

## Proposed Changes

### Data Model & Signup Updates

#### [SignUpActivity.kt](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/java/com/example/paws/ui/screens/auth/SignUpActivity.kt)
- (Confirmed): The code already saves `accountType` (e.g., "Private User", "Animal Shelter") to Firestore. I will ensure the values match the expected filter criteria.

#### [CreatePostFragment.kt](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/java/com/example/paws/ui/screens/home/CreatePostFragment.kt)
- Update `sharePost` and `updatePost`:
    - Fetch the current user's `accountType` before saving the post.
    - Add a `userType` field to the post document in Firestore.

---

### Home Screen Filtering

#### [HomeFragment.kt](file:///Users/tareknaja/Desktop/Uni/Programmazione Mobile/Paws/PawsCode/app/src/main/java/com/example/paws/ui/screens/home/HomeFragment.kt)
- Add a click listener to the filter icon (`ivFilter`).
- Implement a `showFilterDialog()` method:
    - Display an `AlertDialog` with multiple selections (Age, Gender, Type, User Type).
    - Store the selected filters.
- Update `loadGlobalFeed`:
    - Apply client-side filtering to the list of posts fetched from Firestore based on the user's selection.
    - Provide a "Clear Filters" option to reset the view.

---

## Verification Plan

### Manual Verification
1. **Post Creation**:
   - Create a post with a "Private" account and one with a "Shelter" account (if possible, or manually edit DB for testing).
   - Verify that the `userType` field is present in Firestore.
2. **Filter Logic**:
   - Click the filter icon on the Home screen.
   - Select **"Dog"** and **"Male"**.
   - Verify only male dogs are visible.
   - Select **"Animal Shelter"**.
   - Verify only posts from shelters are visible.
   - Clear filters and verify the full feed returns.
