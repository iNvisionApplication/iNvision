document.addEventListener("DOMContentLoaded", () => {
    const notificationBtn = document.getElementById("notificationBtn");
    const notificationPanel = document.getElementById("notificationPanel");
    const notificationBadge = document.getElementById("notificationBadge");
    const notificationList = document.getElementById("notificationList");

    // Exit immediately if the user is an ADMIN or not logged in (elements won't exist)
    if (!notificationBtn || !notificationPanel) return;

    let knownNotificationIds = new Set();
    let isFirstLoad = true;

    // Toggle dropdown panel
    notificationBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        notificationPanel.classList.toggle("hidden");
    });

    // Close panel when clicking outside
    document.addEventListener("click", (e) => {
        if (!notificationBtn.contains(e.target) && !notificationPanel.contains(e.target)) {
            notificationPanel.classList.add("hidden");
        }
    });

    // Initial fetch on page load
    fetchNotifications();

    // Poll for new notifications every 15 seconds
    setInterval(fetchNotifications, 15000);

    // ==========================================
    // CORE LOGIC
    // ==========================================

    function fetchNotifications() {
        fetch("/api/notification")
            .then(response => {
                if (!response.ok) throw new Error("Failed to fetch notifications");
                return response.json();
            })
            .then(notifications => {
                // 1. Update the green dot indicator
                if (notifications.length > 0) {
                    notificationBadge.classList.remove("hidden");
                } else {
                    notificationBadge.classList.add("hidden");
                    notificationList.innerHTML = `<div class="notification-empty">You're all caught up!</div>`;
                }

                // 2. Process notifications
                let htmlContent = "";
                let hasNewNotifications = false;

                notifications.forEach(notif => {
                    // Check if this is a brand new notification arriving while logged in
                    if (!isFirstLoad && !knownNotificationIds.has(notif.notificationId)) {
                        hasNewNotifications = true;
                        // Fire the toast from main.js!
                        if (typeof showToast === "function") {
                            showToast(`New Alert: ${notif.message}`);
                        }
                    }

                    knownNotificationIds.add(notif.notificationId);

                    // Build the HTML for the dropdown
                    htmlContent += `
                        <div class="notification-item" id="notif-${notif.notificationId}">
                            <span class="notification-reason">${formatReason(notif.reason)}</span>
                            <span class="notification-message">${notif.message}</span>
                            <div class="notification-actions">
                                <button type="button" class="btn btn-sm btn-secondary" onclick="markNotificationAsRead(${notif.notificationId})">
                                    Mark as Read
                                </button>
                            </div>
                        </div>
                    `;
                });

                if (notifications.length > 0) {
                    notificationList.innerHTML = htmlContent;
                }

                isFirstLoad = false; // Turn off first load flag
            })
            .catch(error => console.error("Notification Polling Error:", error));
    }

    // Expose markAsRead to the global window so the inline onclick buttons can see it
    window.markNotificationAsRead = function(id) {
        const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
        const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

        // Disable button visually during request
        const notifElement = document.getElementById(`notif-${id}`);
        if(notifElement) {
             notifElement.style.opacity = '0.5';
        }

        // Backend expects the ID as a request parameter based on your controller
        fetch(`/api/notification/read?notificationId=${id}`, {
            method: "PATCH",
            headers: {
                [header]: token
            }
        })
        .then(response => {
            if (response.ok) {
                knownNotificationIds.delete(id);
                fetchNotifications(); // Refresh the list to remove the item and update the dot
            } else {
                if(notifElement) notifElement.style.opacity = '1';
                console.error("Failed to mark as read");
            }
        })
        .catch(error => {
            if(notifElement) notifElement.style.opacity = '1';
            console.error("Network error marking notification as read:", error);
        });
    };

    // Helper to format the ugly ENUM strings into readable text
    function formatReason(reasonString) {
        if (!reasonString) return "Alert";
        return reasonString.replace(/_/g, " ");
    }
});