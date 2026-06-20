# UI Scaling: Home Feed Icons and Labels

This plan addresses the request to enlarge several UI elements (Gender, Breed, Name, Heart, and Info) in the Home Feed puppy cards for better visibility.

## Proposed Changes

### 1. Home Feed Card Scaling
Update dimensions and text sizes in the feed card layout.

#### [item_feed_card.xml](file:///Users/sara/Desktop/3-ANNO/Mobile/Paws/PawsCode/app/src/main/res/layout/item_feed_card.xml)
- **Info Icon (Top Right)**: Increase container from 32dp to 40dp, icon from 16dp to 20dp.
- **Gender Icon**: Increase container from 32dp to 40dp, icon from 16dp to 20dp.
- **Breed Icon**: Increase container from 32dp to 40dp, icon from 20dp to 24dp.
- **Name Capsule**: Increase height from 32dp to 40dp, text size from 14sp to 16sp.
- **Likes Capsule (Heart)**: Increase height from 32dp to 40dp, heart icon from 14dp to 18dp, text size from 13sp to 15sp.

## Verification Plan

### Manual Verification
- **Visual Inspection**: Open the Home screen and compare the new icon sizes with the previous version. They should appear noticeably larger and easier to interact with/read.
- **Alignment**: Ensure that the bottom row (Gender, Breed, Name) and the Likes capsule remain vertically centered and horizontally aligned correctly within their parent containers.
