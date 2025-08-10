function removeTeacher() {
    const regId = document.getElementById('registrationId').value.trim();
    if (!regId) return alert("Please enter registration number");

    fetch(`/institution-admin/manage-teachers/remove/${encodeURIComponent(regId)}`, {
        method: 'DELETE'
    })
        .then(res => res.json())
        .then(data => {
            if (data.error) {
                alert(data.error);
                return;
            }
            if (data.hasActiveCourses) {
                document.getElementById('confirmDialog').style.display = 'block';
                document.getElementById('confirmText').innerText =
                    `Teacher is assigned to ${data.courses.length} courses. Proceed with unassigning and deleting?`;
                window.selectedTeacherId = regId;
            } else {
                alert(data.message);
                location.reload();
            }
        })
        .catch(err => alert("Error: " + err));
}

function unassignAndRemove() {
    fetch(`/institution-admin/manage-teachers/unassign-and-remove?registrationId=${encodeURIComponent(window.selectedTeacherId)}`, {
        method: 'POST'
    })
        .then(res => res.json())
        .then(data => {
            alert(data.message);
            location.reload();
        })
        .catch(err => alert("Error: " + err));
}

function cancelRemove() {
    document.getElementById('confirmDialog').style.display = 'none';
    window.selectedTeacherId = null;
}

document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('success') === 'true') {
        alert('Teacher added successfully!');
    }

    const error = params.get('error');
    if (error) {
        let errorMessage = '';
        switch (error) {
            case 'teacherNotFound':
                errorMessage = 'The teacher you entered does not exist.';
                break;
            case 'courseNotFound':
                errorMessage = 'The selected course was not found.';
                break;
            case 'unauthorized':
                errorMessage = 'You are not authorized to perform this action.';
                break;
            default:
                errorMessage = 'An unexpected error occurred.';
        }
        alert(errorMessage);
    }
});
