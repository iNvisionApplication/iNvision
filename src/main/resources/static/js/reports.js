// Active state store to preserve runtime metrics for instant CSV compiling
let activeInventoryData = [];
let activeLoanData = [];
let activeOverdueData = [];

// Automatically load the first tab's data array on script execution mount
document.addEventListener("DOMContentLoaded", () => {
    if (document.getElementById("inventoryTable")) {
        generateInventoryReport();
    }
});

function switchReportTab(targetTab) {
    document.querySelectorAll('.tab-panel').forEach(panel => panel.style.display = 'none');
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));

    document.getElementById(`${targetTab}TabPanel`).style.display = 'block';
    event.currentTarget.classList.add('active');
}

// ─────────────────────────────────────────────────────────────────────────
// 1. ASSET INVENTORY INTERACTION LOGIC
// ─────────────────────────────────────────────────────────────────────────
function generateInventoryReport() {
    const title = document.getElementById("invTitle").value;
    const category = document.getElementById("invCategory").value;
    const status = document.getElementById("invStatus").value;
    const location = document.getElementById("invLocation").value;

    const queryUrl = `/api/reports/inventory?title=${encodeURIComponent(title)}&category=${category}&status=${status}&location=${location}`;

    fetch(queryUrl)
        .then(res => res.json())
        .then(data => {
            activeInventoryData = data;
            const tbody = document.querySelector("#inventoryTable tbody");
            tbody.innerHTML = "";

            if (data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">No assets match specified filter arrays.</td></tr>`;
                return;
            }

            data.forEach(asset => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td><strong>#${asset.assetId}</strong></td>
                    <td>${asset.title}</td>
                    <td><span class="badge">${asset.category}</span></td>
                    <td><code>${asset.serialNumber || 'N/A'}</code></td>
                    <td>${asset.location}</td>
                    <td>${asset.condition || 'N/A'}</td>
                    <td>R ${Number(asset.cost).toFixed(2)}</td>
                    <td><span class="status-indicator">${asset.status}</span></td>
                `;
                tbody.appendChild(row);
            });
        }).catch(() => alert("Could not fetch inventory metrics."));
}

function exportInventoryCSV() {
    if (activeInventoryData.length === 0) return alert("Generate a report dataset before exporting data.");

    const headers = ["Asset ID", "Title", "Category", "Serial Number", "Location", "Condition", "Cost (ZAR)", "Status"];
    const rows = activeInventoryData.map(a => [
        a.assetId, `"${a.title}"`, a.category, `"${a.serialNumber || 'N/A'}"`, a.location, a.condition, a.cost, a.status
    ]);

    streamCsvBlob("Asset_Inventory_Report", headers, rows);
}

// ─────────────────────────────────────────────────────────────────────────
// 2. LOAN HISTORY INTERACTION LOGIC (UPGRADED)
// ─────────────────────────────────────────────────────────────────────────
function generateLoanReport() {
    // 1. Grab target elements and preserve initial text state
    const submitBtn = document.getElementById("loanSubmitBtn");
    const originalText = submitBtn.innerHTML;

    const assetTitle = document.getElementById("loanAssetTitle").value;
    const borrowerEmail = document.getElementById("loanBorrowerEmail").value;
    const department = document.getElementById("loanDepartment").value;
    const status = document.getElementById("loanStatus").value;

    const queryUrl = `/api/reports/loans?assetTitle=${encodeURIComponent(assetTitle)}` +
                     `&borrowerEmail=${encodeURIComponent(borrowerEmail)}` +
                     `&department=${department}` +
                     `&status=${status}`;

    // 2. Trigger loading loop state mutations
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<span class="btn-spinner"></span> Loading loans...`;

    fetch(queryUrl)
        .then(res => res.json())
        .then(data => {
            activeLoanData = data;
            const tbody = document.querySelector("#loanTable tbody");

            if (!tbody) {
                console.error("DOM Error: Could not find '#loanTable tbody'. Check your dashboard.html file.");
                alert("UI Layout Error: Loan table container is missing.");
                return;
            }

            tbody.innerHTML = "";

            if (data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">No loan history records match specified filter selections.</td></tr>`;
                return;
            }

            data.forEach(loan => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td><strong>#${loan.loanId}</strong></td>
                    <td>${loan.assetTitle || 'System Asset'}</td>
                    <td><code>${loan.userEmail || 'N/A'}</code></td>
                    <td><span class="badge" style="background: var(--bg-base);">${loan.userDepartment || 'N/A'}</span></td>
                    <td>${formatTimestamp(loan.requestDate)}</td>
                    <td>${formatTimestamp(loan.dueDate)}</td>
                    <td>${loan.returnDate ? formatTimestamp(loan.returnDate) : '<em style="color:#10b981; font-weight:500;">Active Open</em>'}</td>
                    <td><strong>${loan.status}</strong></td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(err => {
            console.error("Fetch error:", err);
            alert("Could not isolate historical loan matrix.");
        })
        .finally(() => {
            // 3. Re-enable button interaction and tear down loader elements
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        });
}

function exportLoanCSV() {
    if (activeLoanData.length === 0) return alert("Generate history matrix records before executing data downloads.");

    const headers = ["Loan ID", "Asset Title", "Borrower Email", "Department", "Request Date", "Due Date", "Return Date", "Status"];
    const rows = activeLoanData.map(l => [
        l.loanId, `"${l.assetTitle}"`, l.userEmail, l.userDepartment, l.requestDate, l.dueDate, l.returnDate || "Active Open Assignment", l.status
    ]);

    streamCsvBlob("Loan_History_Report", headers, rows);
}

// ─────────────────────────────────────────────────────────────────────────
// 3. OVERDUE TRACKER INTERACTION LOGIC
// ─────────────────────────────────────────────────────────────────────────
function generateOverdueReport() {
    fetch("/api/reports/overdue")
        .then(res => res.json())
        .then(data => {
            activeOverdueData = data;
            const tbody = document.querySelector("#overdueTable tbody");
            tbody.innerHTML = "";

            if (data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#10b981;">Excellent: All operating assets clear of contract rule milestones.</td></tr>`;
                return;
            }

            data.forEach(loan => {
                const daysOverdue = Math.floor((new Date() - new Date(loan.dueDate)) / (1000 * 60 * 60 * 24));
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>#${loan.loanId}</td>
                    <td><span style="color:#ef4444; font-weight:500;">${loan.assetTitle}</span></td>
                    <td>${loan.userEmail}</td>
                    <td>${formatTimestamp(loan.checkoutDate)}</td>
                    <td>${formatTimestamp(loan.dueDate)}</td>
                    <td><strong style="color:#ef4444;">${daysOverdue > 0 ? daysOverdue : 1} Days Overdue</strong></td>
                    <td><span class="badge" style="background:rgba(239,68,68,0.15); color:#ef4444;">${loan.status}</span></td>
                `;
                tbody.appendChild(row);
            });
        }).catch(() => alert("Could not parse overdue inventory checks."));
}

function exportOverdueCSV() {
    if (activeOverdueData.length === 0) return alert("No operational parameters isolated to structure overdue sheets.");

    const headers = ["Loan ID", "Asset Title", "Borrower Email", "Checkout Date", "Expected Due Date", "Status"];
    const rows = activeOverdueData.map(o => [
        o.loanId, `"${o.assetTitle}"`, o.userEmail, o.checkoutDate, o.dueDate, o.status
    ]);

    streamCsvBlob("Overdue_Risk_Report", headers, rows);
}

// ─────────────────────────────────────────────────────────────────────────
// HELPER UTILITIES: BLOB FORMAT PROCESSING PIPELINES
// ─────────────────────────────────────────────────────────────────────────
function formatTimestamp(isoString) {
    if (!isoString) return "N/A";
    return isoString.replace("T", " ").slice(0, 19);
}

function streamCsvBlob(filenamePrefix, headers, rows) {
    const csvContent = [
        headers.join(","),
        ...rows.map(r => r.join(","))
    ].join("\n");

    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);

    const timeStampMarker = new Date().toISOString().slice(0,10);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", `${filenamePrefix}_${timeStampMarker}.csv`);

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}
// ─────────────────────────────────────────────────────────────────────────
// SECURITY AUDIT TRAIL EXTRACTION ENGINE
// ─────────────────────────────────────────────────────────────────────────
function triggerAuditTrailExtraction() {
    const extractBtn = document.getElementById("auditExtractBtn");
    const entityType = document.getElementById("auditFilterEntityType").value;
    const originalText = extractBtn.innerHTML;

    // Capture security validation metadata from Thymeleaf layout boundaries
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    // Lock interaction and trigger button loading state
    extractBtn.disabled = true;
    extractBtn.innerHTML = `<span class="btn-spinner"></span> Extracting Matrix...`;

    // Direct path targeting your new transactional API endpoint
    const queryUrl = `/api/audit-logs/extract?entityType=${entityType}`;

    fetch(queryUrl, {
        method: "GET",
        headers: {
            [header]: token // Standard security verification handshake
        }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 403) throw new Error("Security Violation: Access Denied.");
            throw new Error("Internal server fault during log generation.");
        }
        return response.json();
    })
    .then(data => {
        if (data.length === 0) {
            alert("No audit logs found matching the selected transaction module.");
            return;
        }

        // Define clean, business-friendly columns for the spreadsheet
        const headers = ["Log ID", "Operator Email", "Module Affected", "Target Record ID", "Action Executed", "Details / Changes", "Timestamp Metric"];

        // Map the server DTO records into compliant CSV string lines
        const rows = data.map(log => [
            log.logId,
            log.operatorEmail,
            log.entityType,
            log.entityId || "SYSTEM",
            log.action,
            `"${(log.details || "").replace(/"/g, '""')}"`, // Safely sanitizes quotes/commas inside descriptions
            formatTimestamp(log.timestamp)
        ]);

        // Compile and stream the text data directly to the user's browser storage
        const csvContent = [headers.join(","), ...rows.map(r => r.join(","))].join("\n");
        const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
        const url = URL.createObjectURL(blob);

        const currentDayMarker = new Date().toISOString().slice(0, 10);
        const downloadAnchor = document.createElement("a");
        downloadAnchor.setAttribute("href", url);
        downloadAnchor.setAttribute("download", `invision_audit_trail_${currentDayMarker}.csv`);
        downloadAnchor.style.visibility = "hidden";

        document.body.appendChild(downloadAnchor);
        downloadAnchor.click();
        document.body.removeChild(downloadAnchor);
        URL.revokeObjectURL(url); // Free up browser allocation resources immediately
    })
    .catch(error => {
        console.error("Audit extraction breakdown:", error);
        alert("Extraction Refused: " + error.message);
    })
    .finally(() => {
        // Unlock button element and restore state layout natively
        extractBtn.disabled = false;
        extractBtn.innerHTML = originalText;
    });
}