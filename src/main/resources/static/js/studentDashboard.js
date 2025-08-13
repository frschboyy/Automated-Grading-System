let selectedCourseId = null;

// Add Course 
document.addEventListener('DOMContentLoaded', () => {
    const addCourseForm = document.getElementById('addCourseForm');
    const courseCodeInput = document.getElementById('courseCode');

    if (addCourseForm) {
        addCourseForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const courseCode = courseCodeInput.value.trim();
            if (!courseCode) {
                alert('Please enter a course code');
                return;
            }
            try {
                const res = await fetch('/api/student/courses/enroll', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ courseCode })
                });
                if (!res.ok) {
                    const errorText = await res.text();
                    throw new Error(`Failed to add course: ${errorText}`);
                }
                alert('Course enrolled successfully');
                window.location.reload();
            } catch (err) {
                alert(err.message);
            }
        });
    }

    const courseLinks = document.querySelectorAll('.course-list a');


    courseLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const courseId = link.dataset.id;
            const courseCode = link.dataset.code;
            const courseName = link.dataset.name;
            loadAssignments(courseId, courseCode, courseName);
        });
    });

    // Determine selected course: query param > sessionStorage > first link
    const urlParams = new URLSearchParams(window.location.search);
    let courseIdToSelect = urlParams.get('courseId') || sessionStorage.getItem('currentCourseId') || null;

    if (!courseIdToSelect && courseLinks.length > 0) {
        courseIdToSelect = courseLinks[0].dataset.id;
    }

    courseLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const courseId = link.dataset.id;
            const courseCode = link.dataset.code;
            const courseName = link.dataset.name;

            sessionStorage.setItem('currentCourseId', courseId);
            loadAssignments(courseId, courseCode, courseName);
        });
    });

    // Auto-select the stored course
    if (courseIdToSelect) {
        const linkToSelect = Array.from(courseLinks).find(link => link.dataset.id === courseIdToSelect);
        if (linkToSelect) {
            sessionStorage.setItem('studentCurrentCourseId', courseIdToSelect);
            loadAssignments(linkToSelect.dataset.id, linkToSelect.dataset.code, linkToSelect.dataset.name);
        }
    }
});

// Fetch Assignments For Course Selected
function loadAssignments(courseId, courseCode, courseName) {
    if (selectedCourseId === courseId) {
        console.log(`Course ${courseId} (${courseCode} - ${courseName}) already selected, skipping fetch.`);
        return;
    }

    console.log(`Loading assignments for course ${courseId}: ${courseCode} - ${courseName}`);

    fetch(`/student-dashboard/assignments?courseId=${courseId}`)
        .then(response => {
            if (!response.ok) throw new Error("Failed to load assignments");
            return response.text();
        })
        .then(html => {
            const container = document.getElementById('assignments-container');
            if (!container) {
                console.error("Assignments container not found");
                return;
            }
            container.innerHTML = html;

            const header = document.getElementById('selectedCourseName');
            if (header) {
                header.innerHTML = `
                    <div class="selected-course-summary">
                        <h4>${courseCode}</h4>
                        <p>${courseName}</p>
                    </div>
                `;
            }

            // Highlight selected course in sidebar
            document.querySelectorAll('.course-list a').forEach(link => {
                link.classList.remove('selected-course');
                if (link.dataset.id == courseId) {
                    link.classList.add('selected-course');
                }
            });

            selectedCourseId = courseId;
        })
        .catch(err => console.error(err));
}
