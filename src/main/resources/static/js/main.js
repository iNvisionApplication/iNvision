// Add these pagination trackers to the top of your script
let currentAssetsPage = 0;
let currentLoansPage = 0;

function getAssetsPageSize() {
    const width = window.innerWidth;
    if (width >= 1600) return 10;
    if (width >= 1300) return 8;
    if (width >= 1000) return 6;
    return 4;
}

function getLoansPageSize() {
    const width = window.innerWidth;
    if (width >= 1400) return 12;
    if (width >= 1100) return 9;
    if (width >= 768) return 6;
    return 4;
}

let assetSearchTimeout = null;

document.addEventListener("DOMContentLoaded", function () {
    // 1. Check for incoming success parameters from incoming redirects
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get("success") === "true") {
        const message = urlParams.get("msg") || "Action processed successfully.";
        showToast(message);
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    // If we are on the edit page, pre-load the asset's current data profiles
    if (document.getElementById("updateAssetForm")) {
        prefillAssetDataFields();
        document.getElementById("updateAssetForm").addEventListener("submit", submitAssetUpdate);
    }

    // Bind CSV Template Downloader
    const downloadTemplateBtn = document.getElementById("downloadTemplateBtn");
    if (downloadTemplateBtn) {
        downloadTemplateBtn.addEventListener("click", downloadCSVTemplate);
    }

    // Bind Manual Asset Creation Form
    const manualAssetForm = document.getElementById("manualAssetForm");
    if (manualAssetForm) {
         manualAssetForm.addEventListener("submit", submitManualAsset);
    }

    // Bind Bulk CSV Import Form
    const bulkUploadForm = document.getElementById("bulkUploadForm");
    if (bulkUploadForm) {
          bulkUploadForm.addEventListener("submit", submitBulkImport);
    }

    setupDueDateLimit();

    if (document.getElementById("loanHistoryBody")) {
        loadUserLoans(currentLoansPage);
    }

    if (document.getElementById("availableAssetsContainer")) {
        loadAvailableAssets(currentAssetsPage);
    }

    if (document.getElementById("usersTableBody")) {
        loadUsers();
    }

    // Mobile Sidebar Toggle
    const mobileMenuBtn = document.getElementById("mobileMenuBtn");
    const sidebar = document.getElementById("sidebar");
    const mobileBackdrop = document.getElementById("mobileBackdrop");

    if (mobileMenuBtn && sidebar && mobileBackdrop) {
        mobileMenuBtn.addEventListener("click", function () {
            sidebar.classList.toggle("open");
            mobileBackdrop.classList.toggle("show");
            const isOpen = sidebar.classList.contains("open");
            mobileMenuBtn.setAttribute("aria-expanded", isOpen ? "true" : "false");
        });

        mobileBackdrop.addEventListener("click", function () {
            sidebar.classList.remove("open");
            mobileBackdrop.classList.remove("show");
            mobileMenuBtn.setAttribute("aria-expanded", "false");
        });
    }

    const loanRequestForm = document.getElementById("loanRequestForm");
    if (loanRequestForm) {
        loanRequestForm.addEventListener("submit", submitLoanRequest);
    }

    const closeBtn = document.getElementById("closeErrorModalBtn");
    if (closeBtn) {
        closeBtn.addEventListener("click", function() {
            const backdrop = document.getElementById("errorModalBackdrop");
            if (backdrop) {
                backdrop.classList.remove("open");
            }
        });
    }
});

function getCurrentUserId() {
    const input = document.getElementById("currentUserId");
    return input ? input.value : null;
}

// ================= LOANS =================

function getCurrentUserRole() {
    const input = document.getElementById("currentUserRole");
    return input ? input.value.toUpperCase() : null;
}

function loadUserLoans(pageNumber) {
    if (typeof pageNumber !== 'number' || isNaN(pageNumber)) {
        pageNumber = 0;
    }
    currentLoansPage = pageNumber;
    const userId = getCurrentUserId();
    const role = getCurrentUserRole();
    const params = new URLSearchParams(window.location.search);
    const statusFilter = params.get("status");
    const tableBody = document.getElementById("loanHistoryBody");
    const tableWrapper = tableBody?.closest('.table-wrapper');

    if (!tableBody) return;

    let url;
    if (role === "ADMIN" || role === "MANAGER") {
        if (statusFilter === "PENDING") {
            url = `/api/loans/status?status=PENDING&page=${pageNumber}&size=${getLoansPageSize()}`;
        } else if (role === "MANAGER") {
            url = `/api/loans/department?page=${pageNumber}&size=${getLoansPageSize()}`;
        } else {
            url = `/api/loans?page=${pageNumber}&size=${getLoansPageSize()}`;
        }
    } else {
        if (!userId) return;
        url = `/api/loans/user/${userId}?page=${pageNumber}&size=${getLoansPageSize()}`;
    }

    fetch(url)
        .then(async response => {
            if (!response.ok) {
                throw new Error(`Server returned status ${response.status}`);
            }
            return response.json();
        })
        .then(pageData => {
            const loans = Array.isArray(pageData) ? pageData : pageData.content;
            const totalPages = pageData.page ? pageData.page.totalPages : (pageData.totalPages || 1);
            const currentPage = pageData.page ? pageData.page.number : (pageData.number || 0);

            tableBody.innerHTML = "";

            if (!Array.isArray(loans) || loans.length === 0) {
                tableBody.innerHTML = `<tr><td colspan="7">No loans found.</td></tr>`;
                removePaginationControls("loansPaginationControls");
                return;
            }

            loans.forEach(loan => {
                const row = document.createElement("tr");
                let actionsHtml = "";

                if (role === "ADMIN" || role === "MANAGER") {
                    if (loan.status === "PENDING") {
                        actionsHtml = `<button class="btn btn-sm btn-success" onclick="approveLoan(${loan.loanId})">Approve</button>`;
                    } else if (loan.assetLoanStatus === "PENDING_RETURN_CONFIRMATION") {
                        actionsHtml = `<button class="btn btn-sm btn-primary" onclick="executeLoanAction(${loan.loanId}, 'confirm-return')">Confirm Return</button>`;
                    } else {
                        actionsHtml = `<span style="font-size:11px; color:var(--text-muted);">${loan.assetLoanStatus || 'Processed'}</span>`;
                    }
                } else {
                    if (loan.status === "APPROVED" && loan.assetLoanStatus === "PENDING_COLLECTION") {
                        actionsHtml = `<button class="btn btn-sm btn-steel" onclick="executeLoanAction(${loan.loanId}, 'collect')">Confirm Collection</button>`;
                    } else if (loan.status === "APPROVED" && loan.assetLoanStatus === "COLLECTED") {
                        actionsHtml = `<button class="btn btn-sm btn-danger" onclick="executeLoanAction(${loan.loanId}, 'return')">Return Asset</button>`;
                    } else if (loan.assetLoanStatus === "PENDING_RETURN_CONFIRMATION") {
                        actionsHtml = `<em style="font-size:12px; color:var(--amber);">Awaiting Manager Sign-off</em>`;
                    } else {
                        actionsHtml = `<span style="font-size:11px; color:var(--text-muted);">No Action</span>`;
                    }
                }

                row.innerHTML = `
                    <td><strong>#${loan.loanId || "N/A"}</strong></td>
                    <td>${loan.assetTitle || "N/A"}</td>
                    <td>${loan.description || "N/A"}</td>
                    <td>${formatDateTime(loan.requestDate)}</td>
                    <td>${formatDateTime(loan.dueDate)}</td>
                    <td>${getStatusBadge(loan.status)}</td>
                    <td class="table-actions">${actionsHtml}</td>
                `;
                tableBody.appendChild(row);
            });

            buildPaginationControls("loansPaginationControls", tableWrapper, totalPages, currentPage, loadUserLoans);
        })
        .catch(error => {
            console.error("Error loading loans:", error);
            tableBody.innerHTML = `<tr><td colspan="7">Failed to load loans.</td></tr>`;
            showErrorModal("System Error", "Failed to retrieve loan history files from the server. Please try again later.");
        });
}

async function approveLoan(loanId) {
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    try {
        const response = await fetch(`/api/loans/${loanId}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                [header]: token
            },
            body: JSON.stringify({ loanStatus: "APPROVED" })
        });

        if (!response.ok) throw new Error();
        loadUserLoans(currentLoansPage);
    } catch (error) {
        showErrorModal("Approval Failed", "Unable to approve this loan request.");
    }
}

function submitLoanRequest(event) {
    event.preventDefault();

    const userId = getCurrentUserId();
    const assetId = document.getElementById("selectedAssetId")?.value;
    const loanPeriod = document.getElementById("loanPeriod")?.value;
    const description = document.getElementById("description")?.value;

    if (!assetId) {
        showErrorModal("Selection Required", "Please select an asset from the active catalog inventory first.");
        return;
    }
    if (!userId) {
        showErrorModal("Identity Error", "User contextual assignment metadata missing.");
        return;
    }
    if (!loanPeriod) {
        showErrorModal("Timeline Required", "Please specify a clear operational loan duration period.");
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
            if (!response.ok) {
                let errorData;
                try {
                    errorData = JSON.parse(responseText);
                } catch (e) {
                    errorData = { message: responseText || `Status ${response.status}: Server operational failure.` };
                }
                throw errorData;
            }
            return responseText ? JSON.parse(responseText) : {};
        })
        .then(() => {
            window.location.href = "/loans";
        })
        .catch(error => {
            if (error.code === "BAD_LOAN_REQUEST") {
                showErrorModal("Active Request Found", "You already have a pending or active loan for this asset.");
            } else {
                showErrorModal("Submission Refused", error.message || "An unresolved network transmission layout conflict has occurred.");
            }
        });
}

function executeLoanAction(loanId, actionEndpoint) {
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    fetch(`/api/loans/${loanId}/${actionEndpoint}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            [header]: token
        }
    })
    .then(response => {
        if (!response.ok) throw new Error("Operational transformation state rejected by server rules.");
        loadUserLoans(currentLoansPage);
    })
    .catch(error => {
        console.error("Workflow transmission failure:", error);
        showErrorModal("Action Aborted", "Could not complete update validation sequence.");
    });
}

// ================= ASSETS =================

function searchAllAssets() {
    const input = document.getElementById("assetSearchInput");
    const container = document.getElementById("availableAssetsContainer");
    const assetCount = document.getElementById("assetCount");

    if (!input || !container) return;

    clearTimeout(assetSearchTimeout);
    assetSearchTimeout = setTimeout(() => {
        const searchText = input.value.trim().toLowerCase();
        if (!searchText) {
            loadAvailableAssets(0);
            return;
        }

        fetch(`/api/assets?page=0&size=1000`)
            .then(response => response.json())
            .then(pageData => {
                const assets = pageData.content || [];
                const filteredAssets = assets.filter(asset => {
                    const searchableText = `
                        ${asset.title || ""} ${asset.serialNumber || ""} ${asset.category || ""}
                        ${asset.condition || ""} ${asset.location || ""} ${asset.status || ""}
                    `.toLowerCase();
                    return searchableText.includes(searchText);
                });

                renderAssetSearchResults(filteredAssets);
                if (assetCount) assetCount.innerText = `${filteredAssets.length} result(s) found`;
                removePaginationControls("assetsPaginationControls");
            })
            .catch(error => {
                console.error("Asset search failed:", error);
                showErrorModal("Search Failed", "Unable to search assets right now.");
            });
    }, 250);
}

function renderAssetSearchResults(assets) {
    const container = document.getElementById("availableAssetsContainer");
    const role = document.getElementById("currentUserRole")?.value || "BORROWER";
    const isAdminOrManager = role === "ADMIN" || role === "MANAGER";

    if (!container) return;
    container.innerHTML = "";

    if (!assets || assets.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <h3>No matching assets</h3>
                <p>Try a different criteria description mapping matrix entry.</p>
            </div>`;
        return;
    }

    assets.forEach(asset => {
        const card = document.createElement("div");
        card.className = "asset-card";
        const imagePath = getAssetImagePath(asset.photoPath);
        const isLoaned = asset.status && asset.status.toUpperCase() === "LOANED";

        const actionButton = isLoaned
            ? `<button type="button" class="asset-title-btn disabled-action" style="cursor: not-allowed; opacity: 0.7; flex: 1; text-align: left;" disabled>${asset.title || "Untitled"} (Borrowed)</button>`
            : `<button type="button" class="asset-title-btn" style="flex: 1; text-align: left;" onclick="openLoanPanel('${asset.assetId}', '${escapeText(asset.title)}')">${asset.title || "Untitled"}</button>`;

        card.innerHTML = `
            <div class="asset-image-wrap">
                <img src="${imagePath}" alt="Asset Photo" class="asset-img" onerror="this.onerror=null; this.src='/uploads/macbook.png';">
                ${getAssetStatusBadge(asset.status)}
            </div>
            <div class="asset-action-row" style="display: flex; align-items: center; justify-content: space-between; padding-right: 16px; gap: 8px;">
                ${actionButton}
                ${isAdminOrManager ? `<a href="/assets/edit/${asset.assetId}" class="btn btn-sm btn-secondary" style="font-size: 11px; padding: 4px 8px; text-decoration: none; white-space: nowrap; display: inline-flex; align-items: center; gap: 4px;">⚙️ Edit</a>` : ''}
            </div>
            <div class="asset-meta">
                <p><strong>Serial Number</strong><span>${asset.serialNumber || "N/A"}</span></p>
                <p><strong>Category</strong><span>${asset.category || "N/A"}</span></p>
                <p><strong>Condition</strong><span>${asset.condition || "N/A"}</span></p>
                <p><strong>Location</strong><span>${asset.location || "N/A"}</span></p>
                <p><strong>Cost</strong><span>R${asset.cost || "0.00"}</span></p>
            </div>`;
        container.appendChild(card);
    });
}

function showErrorModal(title, message) {
    const backdrop = document.getElementById("errorModalBackdrop");
    const titleText = document.getElementById("errorModalTitle");
    const bodyText = document.getElementById("errorModalBody");

    if (backdrop && titleText && bodyText) {
        titleText.innerText = title;
        bodyText.innerText = message;
        backdrop.classList.add("open");
    } else {
        alert(`${title}\n\n${message}`);
    }
}

function submitManualAsset(event) {
    event.preventDefault();
    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    const assetPayload = {
        title: document.getElementById("title").value,
        serialNumber: document.getElementById("serialNumber").value,
        category: document.getElementById("category").value,
        condition: document.getElementById("condition").value,
        location: document.getElementById("location").value,
        cost: Number(document.getElementById("cost").value),
        path: document.getElementById("path").value,
        acquisitionDate: new Date().toISOString().slice(0, 19)
    };

    fetch("/api/assets", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(assetPayload)
    })
    .then(async response => {
        const errorText = await response.text();
        if (response.ok) {
            showToast("Asset registered successfully!");
            setTimeout(() => window.location.href = "/assets", 1500);
        } else {
            try {
                const errorJson = JSON.parse(errorText);
                showErrorModal("Registration Refused", errorJson.message || errorText);
            } catch(e) {
                showErrorModal("Registration Refused", errorText);
            }
        }
    })
    .catch(error => {
        console.error("Error saving asset:", error);
        showErrorModal("Network Interruption", "An error occurred while executing data synchronization with the server.");
    });
}

function submitBulkImport(event) {
    event.preventDefault();
    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    const fileInput = document.getElementById("csvFile");
    if (!fileInput.files.length) {
        showErrorModal("Selection Error", "Please select a valid CSV manifest file first.");
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);

    const executeBulkBtn = document.getElementById("executeBulkBtn");
    executeBulkBtn.disabled = true;
    executeBulkBtn.textContent = "Processing Ingestion...";

    fetch("/api/assets/import", {
        method: "POST",
        headers: { [csrfHeader]: csrfToken },
        body: formData
    })
    .then(async response => {
        const message = await response.text();
        if (response.ok) {
            showToast("Bulk Processing Complete: " + message);
            setTimeout(() => window.location.href = "/dashboard", 1500);
        } else {
            try {
                const errorJson = JSON.parse(message);
                showErrorModal("Import Rejected", errorJson.message || message);
            } catch(e) {
                showErrorModal("Import Rejected", message);
            }
            executeBulkBtn.disabled = false;
            executeBulkBtn.textContent = "Process Spreadsheet Manifest";
        }
    })
    .catch(error => {
        console.error("Error executing bulk upload:", error);
        showErrorModal("Data Drop Detected", "A connectivity drop or internal error stopped your bulk manifest import.");
        executeBulkBtn.disabled = false;
        executeBulkBtn.textContent = "Process Spreadsheet Manifest";
    });
}

function loadAvailableAssets(pageNumber) {
    if (typeof pageNumber !== 'number' || isNaN(pageNumber)) {
        pageNumber = 0;
    }
    currentAssetsPage = pageNumber;
    const container = document.getElementById("availableAssetsContainer");
    const assetCount = document.getElementById("assetCount");

    if (!container) return;

    const role = document.getElementById("currentUserRole")?.value || "BORROWER";
    const isAdminOrManager = role === "ADMIN" || role === "MANAGER";

    fetch(`/api/assets?page=${pageNumber}&size=${getAssetsPageSize()}`)
        .then(async response => {
            if (!response.ok) throw new Error(`Server returned status ${response.status}`);
            return response.json();
        })
        .then(pageData => {
            const assets = pageData.content;
            const totalPages = pageData.page ? pageData.page.totalPages : (pageData.totalPages || 0);
            const currentPage = pageData.page ? pageData.page.number : (pageData.number || 0);
            const totalElements = pageData.page ? pageData.page.totalElements : (pageData.totalElements || 0);

            container.innerHTML = "";

            if (!Array.isArray(assets) || assets.length === 0) {
                container.innerHTML = `<div class="empty-state"><h3>No available assets</h3></div>`;
                if (assetCount) assetCount.innerText = "0 assets";
                removePaginationControls("assetsPaginationControls");
                return;
            }

            if (assetCount) assetCount.innerText = `${totalElements} assets available`;

            assets.forEach(asset => {
                const card = document.createElement("div");
                card.className = "asset-card";
                const imagePath = getAssetImagePath(asset.photoPath);
                const isLoaned = asset.status && asset.status.toUpperCase() === "LOANED";

                const actionButton = isLoaned
                    ? `<button type="button" class="asset-title-btn disabled-action" style="cursor: not-allowed; opacity: 0.7; flex: 1; text-align: left;" disabled>${asset.title || "Untitled Asset"} (Borrowed)</button>`
                    : `<button type="button" class="asset-title-btn" style="flex: 1; text-align: left;" onclick="openLoanPanel('${asset.assetId}', '${escapeText(asset.title)}')">${asset.title || "Untitled Asset"}</button>`;

                card.innerHTML = `
                    <div class="asset-image-wrap">
                        <img src="${imagePath}" alt="Asset" class="asset-img" onerror="this.onerror=null; this.src='/uploads/macbook.png';">
                        ${getAssetStatusBadge(asset.status)}
                    </div>
                    <div class="asset-action-row" style="display: flex; align-items: center; justify-content: space-between; padding-right: 16px; gap: 8px;">
                        ${actionButton}
                        ${isAdminOrManager ? `<a href="/assets/edit/${asset.assetId}" class="btn btn-sm btn-secondary" style="font-size: 11px; padding: 4px 8px; text-decoration: none; white-space: nowrap; display: inline-flex; align-items: center; gap: 4px;">⚙️ Edit</a>` : ''}
                    </div>
                    <div class="asset-meta">
                        <p><strong>Serial Number</strong><span>${asset.serialNumber || "N/A"}</span></p>
                        <p><strong>Category</strong><span>${asset.category || "N/A"}</span></p>
                        <p><strong>Condition</strong><span>${asset.condition || "N/A"}</span></p>
                        <p><strong>Location</strong><span>${asset.location || "N/A"}</span></p>
                        <p><strong>Cost</strong><span>R${asset.cost || "0.00"}</span></p>
                    </div>`;
                container.appendChild(card);
            });

            const loanPanel = document.getElementById("loanRequestPanel");
            if (loanPanel) {
                buildPaginationControls("assetsPaginationControls", loanPanel, totalPages, currentPage, loadAvailableAssets);
            } else {
                buildPaginationControls("assetsPaginationControls", container, totalPages, currentPage, loadAvailableAssets);
            }
        })
        .catch(error => {
            console.error("Error loading assets:", error);
            container.innerHTML = `<div class="empty-state"><h3>Failed to load assets</h3></div>`;
            showErrorModal("Catalog Error", "Failed to load the available asset catalog due to a server connection failure.");
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
        submitBtn.classList.remove("btn-loading");
        submitBtn.textContent = "Submit Request";
    }
}

// ================= USERS =================

function closeLoanPanel() {
    const panel = document.getElementById("loanRequestPanel");
    if (panel) panel.classList.add("hidden");
}

async function loadUsers() {
    const tbody = document.getElementById("usersTableBody");
    if (!tbody) return;

    try {
        const response = await fetch("/api/users");
        if (!response.ok) throw new Error(`Server returned status ${response.status}`);

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
                    <td class="user-actions">
                        <button type="button" class="btn-edit-user" onclick='openEditUserModal(${JSON.stringify(user)})'>Edit</button>
                        <button type="button" class="${user.active ? 'btn-deactivate-user' : 'btn-activate-user'}" onclick="toggleUserStatus(${user.userId}, ${user.active})">${user.active ? 'Deactivate' : 'Activate'}</button>
                    </td>
                </tr>`;
        });
    } catch (error) {
        console.error("Error loading users:", error);
        tbody.innerHTML = `<tr><td colspan="6">Failed to load users</td></tr>`;
        showErrorModal("Data Sync Failure", "Failed to retrieve registered systems users from the database.");
    }
}

async function toggleUserStatus(userId, isActive) {
    const action = isActive ? "deactivate" : "activate";
    if (!confirm(`Are you sure you want to ${action} this user?`)) return;

    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    try {
        const response = await fetch(`/api/users/${userId}/${action}`, {
            method: "PATCH",
            headers: { [header]: token }
        });
        if (!response.ok) throw new Error(`Server status ${response.status}`);
        loadUsers();
    } catch (error) {
        console.error(error);
        showErrorModal("Action Aborted", `Failed to execute ${action} user operation parameters.`);
    }
}

function openEditUserModal(user) {
    document.getElementById("editUserId").value = user.userId;
    document.getElementById("editName").value = user.name || "";
    document.getElementById("editEmail").value = user.email || "";
    document.getElementById("editDepartment").value = user.department || "";
    document.getElementById("editRole").value = user.role || "";
    document.getElementById("editUserModal").classList.remove("hidden");
}

function closeEditUserModal() {
    document.getElementById("editUserModal").classList.add("hidden");
}

document.addEventListener("DOMContentLoaded", () => {
    const editForm = document.getElementById("editUserForm");
    const addAdminForm = document.getElementById("addAdminForm");
    if (editForm) editForm.addEventListener("submit", saveUserChanges);
    if (addAdminForm) addAdminForm.addEventListener("submit", createAdminUser);
});

function openAddAdminModal() {
    document.getElementById("addAdminModal").classList.remove("hidden");
}

function closeAddAdminModal() {
    document.getElementById("addAdminModal").classList.add("hidden");
    document.getElementById("addAdminForm").reset();
}

async function createAdminUser(event) {
    event.preventDefault();
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    const payload = {
        name: document.getElementById("adminName").value,
        email: document.getElementById("adminEmail").value,
        department: document.getElementById("adminDepartment").value,
        password: document.getElementById("adminPassword").value,
        role: "ADMIN"
    };

    try {
        const response = await fetch("/api/users/staff", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [header]: token
            },
            body: JSON.stringify(payload)
        });
        if (!response.ok) throw new Error(`Server returned status ${response.status}`);

        closeAddAdminModal();
        loadUsers();
        showErrorModal("Admin Created", "New admin created successfully. They must reset their password on first login.");
    } catch (error) {
        console.error("Error creating admin:", error);
        showErrorModal("Creation Failed", "Failed to create new admin user identity registry profile inside database bounds.");
    }
}

async function saveUserChanges(event) {
    event.preventDefault();
    const userId = document.getElementById("editUserId").value;
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    const payload = {
        name: document.getElementById("editName").value,
        email: document.getElementById("editEmail").value,
        department: document.getElementById("editDepartment").value,
        role: document.getElementById("editRole").value
    };

    try {
        const response = await fetch(`/api/users/${userId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                [header]: token
            },
            body: JSON.stringify(payload)
        });
        if (!response.ok) throw new Error();
        closeEditUserModal();
        loadUsers();
    } catch (error) {
        showErrorModal("Update Failed", "Unable to update user structural account configuration settings.");
    }
}

// ================= HELPERS =================

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
        headers: { [header]: token }
    })
    .then(response => {
        if (response.ok) {
            timeoutModal.hide();
            resetIdleTimer();
        } else {
            window.location.href = "/login";
        }
    })
    .catch((error) => {
        console.error("Keep alive failure:", error);
        window.location.href = "/login";
    });
}

function buildPaginationControls(controlsId, targetSibling, totalPages, currentPage, navigationCallback) {
    let controlsContainer = document.getElementById(controlsId);
    if (!controlsContainer) {
        controlsContainer = document.createElement("div");
        controlsContainer.id = controlsId;
        controlsContainer.className = "pagination-container";
        targetSibling.parentNode.insertBefore(controlsContainer, targetSibling.nextSibling);
    }

    if (totalPages <= 1) {
        controlsContainer.innerHTML = "";
        return;
    }

    let htmlContent = `<ul class="pagination-list">`;
    htmlContent += `<li class="page-item ${currentPage === 0 ? 'disabled' : ''}"><button class="page-link" type="button" data-page="${currentPage - 1}">Previous</button></li>`;

    for (let i = 0; i < totalPages; i++) {
        htmlContent += `<li class="page-item ${i === currentPage ? 'active' : ''}"><button class="page-link" type="button" data-page="${i}">${i + 1}</button></li>`;
    }

    htmlContent += `<li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}"><button class="page-link" type="button" data-page="${currentPage + 1}">Next</button></li></ul>`;
    controlsContainer.innerHTML = htmlContent;

    controlsContainer.querySelectorAll(".page-link").forEach(button => {
        button.addEventListener("click", function () {
            const TargetPage = parseInt(this.getAttribute("data-page"));
            const parentLi = this.parentElement;
            if (parentLi.classList.contains("disabled") || parentLi.classList.contains("active")) return;
            navigationCallback(TargetPage);
        });
    });
}

function removePaginationControls(controlsId) {
    const controlsContainer = document.getElementById(controlsId);
    if (controlsContainer) controlsContainer.remove();
}

function downloadCSVTemplate(event) {
    event.preventDefault();
    const headers = ["title", "category", "serial_number", "acquisition_date", "cost", "location", "condition", "status", "photo_path"];
    const sampleRow = ["MacBook Pro 16-inch M4", "AUDIO", "SN-INV778899", "2026-06-18 10:00:00", "45000.00", "AdminOffice", "GOOD", "AVAILABLE", "https://images.unsplash.com/photo-1517336714731-489689fd1ca8"];

    const csvContent = [headers.join(","), sampleRow.join(",")].join("\n");
    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);

    const temporaryLink = document.createElement("a");
    temporaryLink.setAttribute("href", url);
    temporaryLink.setAttribute("download", "invision_asset_import_template.csv");
    temporaryLink.style.visibility = "hidden";

    document.body.appendChild(temporaryLink);
    temporaryLink.click();
    document.body.removeChild(temporaryLink);
    URL.revokeObjectURL(url);
}

function prefillAssetDataFields() {
    const assetId = document.getElementById("targetAssetId").value;

    fetch(`/api/assets/${assetId}`)
        .then(response => {
            if (!response.ok) throw new Error("Failed to resolve asset record data profiles.");
            return response.json();
        })
        .then(asset => {
            document.getElementById("title").value = asset.title || "";
            document.getElementById("serialNumber").value = asset.serialNumber || "";
            document.getElementById("category").value = asset.category || "";
            document.getElementById("condition").value = asset.condition || "";
            document.getElementById("location").value = asset.location || "";
            document.getElementById("cost").value = asset.cost || 0.00;
            document.getElementById("path").value = asset.photoPath || asset.path || "";
        })
        .catch(error => {
            console.error("Error loading asset details:", error);
            showErrorModal("Data Sync Failure", "Could not load current asset profiles for modifications layout mapping.");
        });
}

function submitAssetUpdate(event) {
    event.preventDefault();
    const assetId = document.getElementById("targetAssetId").value;
    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    const updatedPayload = {
        title: document.getElementById("title").value,
        serialNumber: document.getElementById("serialNumber").value,
        category: document.getElementById("category").value,
        condition: document.getElementById("condition").value,
        location: document.getElementById("location").value,
        cost: Number(document.getElementById("cost").value),
        path: document.getElementById("path").value,
        acquisitionDate: new Date().toISOString().slice(0, 19)
    };

    fetch(`/api/assets/update/${assetId}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(updatedPayload)
    })
    .then(async response => {
        const textMessage = await response.text();

        if (response.ok) {
            showToast("System Record Confirmed: " + textMessage);
            setTimeout(() => window.location.href = `/assets?success=true`, 1500);
        } else {
            try {
                const errorJson = JSON.parse(textMessage);
                if (errorJson.message) {
                    const cleanMessage = errorJson.message.replace("An unexpected error occurred: ", "");
                    showErrorModal("Update Refused", cleanMessage);
                } else {
                    showErrorModal("Update Refused", textMessage);
                }
            } catch (e) {
                showErrorModal("Update Refused", textMessage);
            }
        }
    })
    .catch(error => {
        console.error("Error committing PUT update transaction matrix:", error);
        showErrorModal("Network Interruption", "A system connectivity breakdown blocked processing modifications updates.");
    });
}

function showToast(message) {
    let toastContainer = document.getElementById("toastContainer");
    if (!toastContainer) {
        toastContainer = document.createElement("div");
        toastContainer.id = "toastContainer";
        toastContainer.className = "toast-container";
        toastContainer.style.cssText = `
            position: fixed !important;
            top: 24px !important;
            right: 24px !important;
            z-index: 999999 !important;
            display: flex !important;
            flex-direction: column !important;
            gap: 12px !important;
            pointer-events: none !important;
            width: auto !important;
            height: auto !important;
        `;
        document.body.appendChild(toastContainer);
    }

    const toast = document.createElement("div");
    toast.className = "toast-notification toast-success";
    toast.style.cssText = `
        width: 340px !important;
        max-width: calc(100vw - 48px) !important;
        box-sizing: border-box !important;
        pointer-events: auto !important;
    `;

    toast.innerHTML = `
        <span class="toast-icon">✓</span>
        <span class="toast-message" style="white-space: normal !important; word-break: break-word !important;">${message}</span>
    `;
    toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.classList.add("toast-fade-out");
        toast.addEventListener("animationend", () => toast.remove());
    }, 4000);
}