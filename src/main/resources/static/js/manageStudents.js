function removeStudent() {
    const regId = document.getElementById("studentRegistrationId").value.trim();
    if (!regId) {
        alert("Please enter a student registration ID.");
        return;
    }

    const confirmation = confirm(`Are you sure you want to remove student with Registration ID: ${regId}?`);
    if (!confirmation) {
        return; // user cancelled
    }

    fetch(`/institution-admin/manage-students/remove/${regId}`, {
        method: "DELETE"
    })
        .then(response => {
            if (response.ok) {
                alert("Student removed successfully.");
                location.reload();
            } else {
                response.text().then(msg => alert(`Error: ${msg}`));
            }
        })
        .catch(error => {
            console.error("Error removing student:", error);
            alert("An error occurred while removing the student.");
        });
}