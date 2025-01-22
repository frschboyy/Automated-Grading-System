// Fetch submissions
function fetchSubmissions() {
    fetch(`/submissions/existing`)
        .then(response => response.json())
        .then(data => {
            const assignmentsContainer = document.querySelector('.submission-list');
            assignmentsContainer.innerHTML = ''; // Clear existing content 

            // Loop through the submissions and create a list item for each one
            data.forEach(submission => {
                const li = document.createElement('li');
                li.classList.add('submission-item');
                li.innerHTML = `
                    <div class="details">
                        <div class="student-name">Name: ${submission.studentName}</div>
                        <div class="student-email">Email: ${submission.studentEmail}</div>            
                    </div>
                    <button class="btn-checkGrade" onclick="goToEvaluation('${submission.assignmentId}','${submission.studentId}')">View Evaluation</button>
                `;

                assignmentsContainer.appendChild(li);
            });
        })
        .catch(error => {
            console.error('Error fetching submissions:', error);
        });
}

fetchSubmissions();

// Redirect to the evaluations page
function goToEvaluation(assignmentId, studentId) {
    fetch(`/submissions/pushEvaluationDetails?assignmentId=${assignmentId}&studentId=${studentId}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then((response) => {
            if (response.ok) {
                window.location.href = "/evaluation-page";
            } else {
                throw new Error("Failed to save assignment details.");
            }
        })
        .catch((error) => {
            console.error("Error:", error);
            alert("An error occurred while saving assignment details.");
        });
}