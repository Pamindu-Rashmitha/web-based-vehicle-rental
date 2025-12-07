# Reviews Integration Guide

## Quick Integration Steps

I've created the reviews backend, but need to integrate it into browse.html. Here's how:

### Step 1: Add Reviews HTML to Modal

In `browse.html`, find line ~405 (after the `</ul>` that closes vehicle details list).

**Add this HTML INSIDE the modal body:**

```html
<!-- Reviews Section - ADD THIS -->
<div id="vehicleReviews" style="margin-top: 30px;">
    <h6 style="color: #0b3d91; margin-bottom: 20px;">
        <i class="fa fa-star mr-2"></i>Customer Reviews
    </h6>
    
    <!-- Rating Summary -->
    <div id="ratingSummary" style="display: none; background: #f8f9fa; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
        <div style="display: flex; align-items: center; gap: 20px;">
            <div style="text-align: center;">
                <div id="avgRating" style="font-size: 2.5rem; font-weight: 700; color: #ff7b00;">0.0</div>
                <div id="avgStars" style="color: #ff7b00; font-size: 1.2rem;"></div>
                <div id="totalReviews" style="color: #666; font-size: 0.9rem;">0 reviews</div>
            </div>
            <div style="flex: 1;">
                <div id="ratingBars"></div>
            </div>
        </div>
    </div>
    
    <!-- Reviews List -->
    <div id="reviewsList">
        <div style="text-align: center; padding: 20px; color: #999;">
            <i class="fa fa-comment-slash fa-2x mb-2" style="color: #ddd;"></i>
            <p>No reviews yet</p>
        </div>
    </div>
</div>
```

### Step 2: Call loadVehicleReviews()

In `browse.html`, find line ~423 where it shows the modal:

```javascript
$('#vehicleDetailModal').modal('show');
```

**Add this line right after:**

```javascript
loadVehicleReviews(vehicle.id);  // ADD THIS LINE
```

### Step 3: Add JavaScript Functions

Copy ALL the code from `reviews_integration_snippet.js` and paste it at the END of the `<script>` section in browse.html (before the `</script>` closing tag, around line 443).

---

## OR Use This Complete Replacement

Alternatively, I can provide you with a complete updated browse.html file. Would you prefer that?

---

## Testing

After integration:
1. Click on any vehicle card
2. Modal should open
3. Scroll down to see "Customer Reviews" section
4. Initially shows "No reviews yet"
5. Once users submit reviews, they'll appear here with star ratings

The reviews system is working on the backend - just needs this frontend integration!
