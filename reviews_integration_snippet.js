// Add these review functions to browse.html before the closing </script> tag

// Load and display reviews for a vehicle
async function loadVehicleReviews(vehicleId) {
    try {
        const response = await fetch(`/api/reviews/vehicle/${vehicleId}`);
        const data = await response.json();

        if (data.statistics && data.statistics.totalReviews > 0) {
            displayRatingSummary(data.statistics);
            displayReviewsList(data.reviews);
        }
    } catch (error) {
        console.error('Error loading reviews:', error);
    }
}

function displayRatingSummary(stats) {
    document.getElementById('ratingSummary').style.display = 'block';
    document.getElementById('avgRating').textContent = stats.averageRating.toFixed(1);
    document.getElementById('avgStars').innerHTML = generateStars(stats.averageRating);
    document.getElementById('totalReviews').textContent = `${stats.totalReviews} review${stats.totalReviews !== 1 ? 's' : ''}`;

    const total = stats.totalReviews;
    let barsHTML = '';
    const ratings = [
        { stars: 5, count: stats.fiveStars },
        { stars: 4, count: stats.fourStars },
        { stars: 3, count: stats.threeStars },
        { stars: 2, count: stats.twoStars },
        { stars: 1, count: stats.oneStar }
    ];

    ratings.forEach(r => {
        const percent = total > 0 ? (r.count / total) * 100 : 0;
        barsHTML += `
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 5px;">
                <span style="min-width: 50px; font-size: 0.85rem; color: #666;">${r.stars} ⭐</span>
                <div style="flex: 1; height: 6px; background: #e0e0e0; border-radius: 10px;">
                    <div style="height: 100%; background: #ff7b00; width: ${percent}%;"></div>
                </div>
                <span style="min-width: 30px; text-align: right; font-size: 0.8rem; color: #999;">${r.count}</span>
            </div>
        `;
    });

    document.getElementById('ratingBars').innerHTML = barsHTML;
}

function displayReviewsList(reviews) {
    if (reviews.length === 0) return;

    const reviewsHTML = reviews.map(review => `
        <div style="border-bottom: 1px solid #e0e0e0; padding: 15px 0;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                <div>
                    <span style="font-weight: 600; color: #0b3d91;">
                        <i class="fa fa-user-circle mr-1"></i>${escapeHtml(review.user.username)}
                    </span>
                    <span style="color: #ff7b00; margin-left: 10px;">${generateStars(review.rating)}</span>
                </div>
                <span style="color: #999; font-size: 0.85rem;">${formatReviewDate(review.createdDate)}</span>
            </div>
            <div style="color: #333;">${escapeHtml(review.comment || 'No comment provided.')}</div>
        </div>
    `).join('');

    document.getElementById('reviewsList').innerHTML = reviewsHTML;
}

function generateStars(rating) {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    let stars = '';

    for (let i = 0; i < fullStars; i++) {
        stars += '<i class="fas fa-star"></i>';
    }
    if (hasHalfStar) {
        stars += '<i class="fas fa-star-half-alt"></i>';
    }
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
    for (let i = 0; i < emptyStars; i++) {
        stars += '<i class="far fa-star"></i>';
    }
    return stars;
}

function formatReviewDate(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
    return date.toLocaleDateString();
}
