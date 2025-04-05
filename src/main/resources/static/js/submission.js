/* global alert, DataTransfer */

// Navigate back to the dashboard
function backToDashboard() {
    window.location.href = "/dashboard";
}

// Trigger file input when upload area or button is clicked
function triggerFileInput() {
    document.getElementById("file-input").click();
}

// Handle file uploads
function handleFileUpload(event) {
    const fileInput = document.getElementById("file-input");
    const fileList = document.getElementById("file-list");
    fileList.innerHTML = "";

    const allowedExtensions = ["pdf", "doc", "docx"];

    Array.from(event.target.files).forEach((file) => {
        const fileExtension = file.name.split(".").pop().toLowerCase();

        if (!allowedExtensions.includes(fileExtension)) {
            alert(`File type not allowed: ${file.name}`);
            return;
        }

        const fileItem = document.createElement("div");
        fileItem.classList.add("file-item");

        const fileInfo = document.createElement("div");
        fileInfo.classList.add("file-info");
        fileInfo.innerHTML = `<span>📄</span><span>${file.name}</span><span>(${(file.size / (1024 * 1024)).toFixed(2)} MB)</span>`;

        const removeButton = document.createElement("button");
        removeButton.classList.add("btn", "btn-secondary");
        removeButton.textContent = "Remove";

        removeButton.addEventListener("click", () => {
            fileItem.remove();

            const dataTransfer = new DataTransfer();
            Array.from(fileInput.files).forEach((f) => {
                if (f !== file) dataTransfer.items.add(f);
            });
            fileInput.files = dataTransfer.files;
        });

        fileItem.appendChild(fileInfo);
        fileItem.appendChild(removeButton);
        fileList.appendChild(fileItem);
    });
}

// Submit assignment
function submitFiles() {
    const submitButton = document.getElementById("submit-btn");
    const loading = document.getElementById("loader");
    const fileInput = document.getElementById("file-input");

    if (fileInput.files.length === 0) {
        alert("Please upload at least one file before submitting.");
        return;
    }

    submitButton.style.display = "none";
    loading.style.display = "block";

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);

    fetch("/submissions/evaluate", {
        method: "POST",
        body: formData
    })
        .then((response) => {
            if (!response.ok) {
                return response.json().then((data) => {
                    throw new Error(data.error || "Failed to upload file.");
                });
            }
            return response.json();
        })
        .then((data) => {
            loading.style.display = "none";
            if (data.message) {
                alert(`Success: ${data.message}`);
                window.location.href = "/dashboard";
            } else {
                alert("Unknown server response.");
            }
        })
        .catch((error) => {
            loading.style.display = "none";
            submitButton.style.display = "inline";
            alert(`Error: ${error.message}`);
        });
}

// DOMContentLoaded to attach event listeners
document.addEventListener("DOMContentLoaded", () => {
    const fileInput = document.getElementById("file-input");
    const uploadZone = document.querySelector(".upload-zone");
    const submitBtn = document.getElementById("submit-btn");
    const backBtn = document.querySelector(".btn.btn-secondary");

    if (uploadZone) {
        uploadZone.addEventListener("click", triggerFileInput);
    }

    if (fileInput) {
        fileInput.addEventListener("change", handleFileUpload);
    }

    if (submitBtn) {
        submitBtn.addEventListener("click", submitFiles);
    }

    // Back to Dashboard
    if (backBtn && backBtn.textContent.includes("Dashboard")) {
        backBtn.addEventListener("click", backToDashboard);
    }
});
