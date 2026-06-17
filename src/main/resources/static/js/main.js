document.addEventListener("DOMContentLoaded", function () {
    setupDueDateLimit();

    if (document.getElementById("loanHistoryBody")) {
        loadUserLoans();
    }

    if (document.getElementById("availableAssetsContainer")) {
        loadAvailableAssets();
    }

    if (document.getElementById("usersTableBody")) {
        loadUsers();
    }

    const loanRequestForm = document.getElementById("loanRequestForm");
    if (loanRequestForm) {
        loanRequestForm.addEventListener("submit", submitLoanRequest);
    }
});

function getCurrentUserId() {
    const input = document.getElementById("currentUserId");
    return input ? input.value : null;
}

function getCurrentUserRole() {
    const input = document.getElementById("currentUserRole");
    return input ? input.value.toUpperCase() : null;
}

// ================= LOANS =================

function loadUserLoans() {
    const userId = getCurrentUserId();
    const role = getCurrentUserRole();
    const tableBody = document.getElementById("loanHistoryBody");

    if (!tableBody) return;

    let url;
    if (role === "ADMIN" || role === "MANAGER") {
        url = "/api/loans";
    } else {
        if (!userId) return;
        url = `/api/loans/user/${userId}`;
    }

    fetch(url)
        .then(response => {
            if (!response.ok) throw new Error("Failed to fetch loans");
            return response.json();
        })
        .then(loans => {
            tableBody.innerHTML = "";

            if (!Array.isArray(loans) || loans.length === 0) {
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="6">No loans found.</td>
                    </tr>
                `;
                return;
            }

            loans.forEach(loan => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${loan.loanId || "N/A"}</td>
                    <td>${loan.assetTitle || "N/A"}</td>
                    <td>${loan.description || "N/A"}</td>
                    <td>${formatDateTime(loan.requestDate)}</td>
                    <td>${loan.loanPeriod || formatDateTime(loan.dueDate) || "N/A"}</td>
                    <td>${getStatusBadge(loan.status)}</td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error("Error loading loans:", error);
            tableBody.innerHTML = `
                <tr>
                    <td colspan="6">Failed to load loans.</td>
                </tr>
            `;
        });
}

function submitLoanRequest(event) {
    event.preventDefault();

    const userId = getCurrentUserId();
    const assetId = document.getElementById("selectedAssetId")?.value;
    const loanPeriod = document.getElementById("loanPeriod")?.value;
    const description = document.getElementById("description")?.value;

    if (!assetId) {
        alert("Please select an asset first.");
        return;
    }
    if (!userId) {
        alert("User ID is missing.");
        return;
    }
    if (!loanPeriod) {
        alert("Please select a loan period.");
        return;
    }

    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    const loanRequest = {
        assetId: Number(assetId),
        userId: Number(userId),
        description: description,
        loanPeriod: loanPeriod
    };

    fetch("/api/loans", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-User-Id": userId,
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(loanRequest)
    })
        .then(async response => {
            const responseText = await response.text();
            if (!response.ok) throw new Error(responseText);
            return responseText ? JSON.parse(responseText) : {};
        })
        .then(() => {
            alert("Loan request submitted successfully.");
            window.location.href = "/loans";
        })
        .catch(error => {
            console.error("Error submitting loan request:", error);
            alert("Failed to submit loan request.");
        });
}

// ================= ASSETS =================

function loadAvailableAssets() {
    const container = document.getElementById("availableAssetsContainer");
    const assetCount = document.getElementById("assetCount");

    if (!container) return;

    fetch("/api/assets")
        .then(response => {
            if (!response.ok) throw new Error("Failed to fetch assets");
            return response.json();
        })
        .then(assets => {
            container.innerHTML = "";

            if (!Array.isArray(assets) || assets.length === 0) {
                container.innerHTML = `
                    <div class="empty-state">
                        <h3>No available assets</h3>
                        <p>There are currently no assets available for loan.</p>
                    </div>
                `;
                if (assetCount) assetCount.innerText = "0 assets";
                return;
            }

            if (assetCount) assetCount.innerText = `${assets.length} assets`;

            assets.forEach(asset => {
                const card = document.createElement("div");
                card.className = "asset-card";
                const imagePath = getAssetImagePath(asset.path);

                card.innerHTML = `
                    <div class="asset-image-wrap">
                        <img src="${imagePath}" alt="Asset Photo" class="asset-img" onerror="this.onerror=null; this.src='/uploads/macbook.png';">
                        ${getAssetStatusBadge(asset.status)}
                    </div>
                    <button type="button" class="asset-title-btn" onclick="openLoanPanel('${asset.assetId}', '${escapeText(asset.title)}')">
                        ${asset.title || "Untitled Asset"}
                    </button>
                    <div class="asset-meta">
                        <p><strong>Serial Number</strong><span>${asset.serialNumber || "N/A"}</span></p>
                        <p><strong>Category</strong><span>${asset.category || "N/A"}</span></p>
                        <p><strong>Condition</strong><span>${asset.condition || "N/A"}</span></p>
                        <p><strong>Location</strong><span>${asset.location || "N/A"}</span></p>
                        <p><strong>Cost</strong><span>R${asset.cost || "0.00"}</span></p>
                    </div>
                `;
                container.appendChild(card);
            });
        })
        .catch(error => {
            console.error("Error loading assets:", error);
            container.innerHTML = `
                <div class="empty-state">
                    <h3>Failed to load assets</h3>
                    <p>Please check the assets API endpoint.</p>
                </div>
            `;
            if (assetCount) assetCount.innerText = "Error";
        });
}

function openLoanPanel(assetId, assetTitle) {
    const selectedAssetId = document.getElementById("selectedAssetId");
    const selectedAssetTitle = document.getElementById("selectedAssetTitle");
    const submitBtn = document.getElementById("submitLoanBtn");

    if (selectedAssetId) selectedAssetId.value = assetId;
    if (selectedAssetTitle) selectedAssetTitle.innerText = assetTitle;
    if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.innerText = "Submit Request";
    }
}

function closeLoanPanel() {
    const panel = document.getElementById("loanRequestPanel");
    if (panel) panel.classList.add("hidden");
}

// ================= USERS =================

async function loadUsers() {
    const tbody = document.getElementById("usersTableBody");
    if (!tbody) return;

    try {
        const response = await fetch("/api/users");
        if (!response.ok) throw new Error("Failed to fetch users");

        const users = await response.json();
        tbody.innerHTML = "";

        if (!Array.isArray(users) || users.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6">No users found</td></tr>`;
            return;
        }

        users.forEach(user => {
            tbody.innerHTML += `
                <tr>
                    <td>${user.userId || "N/A"}</td>
                    <td>${user.name || "N/A"}</td>
                    <td>${user.email || "N/A"}</td>
                    <td>${user.department || "-"}</td>
                    <td>${user.role || "N/A"}</td>
                    <td>
                        <button type="button" onclick="editUser(${user.userId})">Edit</button>
                        <button type="button" onclick="deleteUser(${user.userId})">Deactivate</button>
                    </td>
                </tr>
            `;
        });
    } catch (error) {
        console.error("Error loading users:", error);
        tbody.innerHTML = `<tr><td colspan="6">Failed to load users</td></tr>`;
    }
}

async function deleteUser(userId) {
    const confirmDelete = confirm("Are you sure you want to deactivate this user?");
    if (!confirmDelete) return;

    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    try {
        const response = await fetch(`/api/users/${userId}`, {
            method: "DELETE",
            headers: {
                [header]: token
            }
        });

        if (!response.ok) throw new Error("Failed to delete user");

        alert("User account successfully deactivated.");
        loadUsers();
    } catch (error) {
        console.error("Error deleting user:", error);
        alert("Failed to deactivate user.");
    }
}

function editUser(userId) {
    alert("Edit form still needs to be added for user ID: " + userId);
}

// ================= HELPERS & SESSION =================

function getAssetImagePath(path) {
    if (!path || path === "string" || path === "url_photo") return "/images/no-image.png";
    if (path.startsWith("http://") || path.startsWith("https://")) return path;
    if (path.startsWith("/")) return path;
    return "/uploads/" + path;
}

function setupDueDateLimit() {
    const dueDateInput = document.getElementById("dueDate");
    if (!dueDateInput) return;

    const today = new Date();
    const maxDate = new Date();
    maxDate.setDate(today.getDate() + 90);

    dueDateInput.min = today.toISOString().split("T")[0];
    dueDateInput.max = maxDate.toISOString().split("T")[0];
}

function formatDateTime(value) {
    if (!value) return "N/A";
    return value.replace("T", " ").substring(0, 16);
}

function getStatusBadge(status) {
    if (!status) return `<span class="status-badge">N/A</span>`;
    return `<span class="status-badge badge-${status.toLowerCase()}">${status}</span>`;
}

function getAssetStatusBadge(status) {
    if (!status) return `<span class="badge badge-retired">N/A</span>`;
    return `<span class="badge badge-${status.toLowerCase()}">${status}</span>`;
}

function escapeText(text) {
    return String(text || "")
        .replace(/\\/g, "\\\\")
        .replace(/'/g, "\\'")
        .replace(/"/g, "&quot;");
}

function extendUserSession() {
    clearInterval(countdownInterval);

    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    fetch('/api/auth/keep-alive', {
        method: 'GET',
        headers: {
            [header]: token
        }
    })
    .then(response => {
        if (response.ok) {
            timeoutModal.hide();
            resetIdleTimer();
        } else {
            window.location.href = "/login";
        }
    })
    .catch(() => {
        window.location.href = "/login";
    });
}