// Sidebar
fetch('/teacher/courses').then(res => res.json()).then(courses => {
  const courseList = document.getElementById('courseList');
  courseList.innerHTML = '';
  courses.forEach(c => {
    const li = document.createElement('li');
    li.innerHTML = `<a href="#" class="course-tab" data-id="${c.id}">${c.courseCode}</a>`;
    courseList.appendChild(li);
  });
  document.querySelectorAll('.course-tab').forEach(tab => {
    tab.addEventListener('click', e => {
      e.preventDefault();
      loadCourse(tab.dataset.id, tab.textContent);
    });
  });
});

// Courses
function loadCourse(courseId, courseCode) {
  document.getElementById('selectedCourseName').textContent = `Course: ${courseCode}`;
  document.getElementById('currentCourseId').value = courseId;

  // set hidden field for assignment form
  document.getElementById('courseId').value = courseId;

  document.getElementById('courseContent').classList.remove('hidden');
  loadAssignments(courseId);
  loadStudents(courseId);
}

// Assignments
function loadAssignments(courseId) {
  fetch(`/teacher/assignments/course/${courseId}`)
    .then(res => res.json())
    .then(data => renderAssignments(data));
}

// Render Assignments in UI
function renderAssignments(assignments) {
  const container = document.getElementById('assignmentList');
  container.innerHTML = '';
  assignments.forEach(a => {
    const li = document.createElement('li');
    li.classList.add('assignment-item');
    li.innerHTML = `
                <div>
                    <h4>${a.title}</h4>
                    <p>${a.description}</p>
                    <small>Due: ${a.dueDate}</small>
                </div>
                <div>
                    <a href="/download?file=${encodeURIComponent(a.assignmentFileUrl)}" class="download-link" title="Download Assignment" target="_blank">
                      <i class="fas fa-download"></i>
                    </a>
                    <a href="/teacher/assignments/${a.id}/submissions" class="button-link">
                      View Submissions
                    </a>
                    <button class="btn btn-edit" data-id="${a.id}">Edit</button>
                    <button class="btn btn-delete" data-id="${a.id}">Delete</button>
                </div>`;
    container.appendChild(li);
  });
  container.querySelectorAll('.btn-edit').forEach(btn =>
    btn.addEventListener('click', () => editAssignment(btn.dataset.id)));
  container.querySelectorAll('.btn-delete').forEach(btn =>
    btn.addEventListener('click', () => deleteAssignment(btn.dataset.id)));
}

function clearAssignmentForm() {
  const form = document.querySelector('.assignment-form');
  if (form) {
    form.reset();
    document.getElementById('rubricWeightValue').innerText =
      form.querySelector('#rubricWeight').value;
  }
}

// Students
function loadStudents(courseId) {
  fetch(`/teacher/students/course/${courseId}`).then(res => res.json()).then(data => {
    const container = document.getElementById('studentList');
    container.innerHTML = '';
    data.forEach(s => {
      const li = document.createElement('li');
      li.textContent = `${s.name} [ ${s.registrationId} ]`;
      container.appendChild(li);
    });
  });
}

// Edit Assignment
function editAssignment(assignmentId) {
  fetch(`/teacher/assignments/${assignmentId}`).then(res => res.json()).then(assignment => {
    document.getElementById('editId').value = assignment.id;
    document.getElementById('editTitle').value = assignment.title;
    document.getElementById('editDescription').value = assignment.description;
    document.getElementById('editDueDate').value = assignment.dueDate;
    document.getElementById('editRubricWeight').value = assignment.rubricWeight;
    document.getElementById('editAssignmentModal').classList.add('visible');
  });
}

document.getElementById('editAssignmentForm').addEventListener('submit', e => {
  e.preventDefault();
  const id = document.getElementById('editId').value;
  const formData = new FormData();
  formData.append("title", document.getElementById('editTitle').value);
  formData.append("description", document.getElementById('editDescription').value);
  formData.append("dueDate", document.getElementById('editDueDate').value);
  formData.append("rubricWeight", document.getElementById('editRubricWeight').value);

  // Add rubric file if selected
  const rubricFile = document.getElementById('editRubricFile').files[0];
  if (rubricFile) formData.append("rubricDocument", rubricFile);

  // Add assignment file if selected
  const assignmentFile = document.getElementById('editAssignmentFile').files[0];
  if (assignmentFile) formData.append("assignmentDocument", assignmentFile);

  fetch(`/teacher/assignments/${id}`, { method: 'PUT', body: formData }).then(res => {
    if (res.ok) {
      alert('Assignment updated');
      closeEditModal();
      loadAssignments(document.getElementById('currentCourseId').value);
    } else alert('Error updating assignment');
  });
});


function closeEditModal() {
  document.getElementById('editAssignmentModal').classList.remove('visible');
}

document.getElementById('editAssignmentModal').addEventListener('click', (e) => {
  if (e.target === e.currentTarget) {
    closeEditModal();
  }
});

// Delete Assignment
function deleteAssignment(assignmentId) {
  if (!confirm('Are you sure you want to delete this assignment?')) return;
  fetch(`/teacher/assignments/${assignmentId}`, { method: 'DELETE' }).then(res => {
    if (res.ok) {
      alert('Assignment deleted');
      loadAssignments(document.getElementById('currentCourseId').value);
    } else alert('Error deleting assignment');
  });
}

// Submit createAssignment Form
document.getElementById('createAssignmentForm').addEventListener('submit', async function (e) {
  e.preventDefault(); // prevent normal form submit

  const form = e.target;
  const submitButton = form.querySelector('button[type="submit"]');

  // Disable the button immediately
  submitButton.disabled = true;
  submitButton.innerText = 'Creating...';

  const formData = new FormData(form);

  try {
    const response = await fetch(form.action, {
      method: form.method,
      body: formData,
      headers: {
      }
    });

    if (response.ok) {
      alert('Assignment created successfully');
      // Re-enable
      submitButton.disabled = false;
      submitButton.innerText = 'Create Assignment';

      form.reset(); // clear the form
      document.getElementById('rubricWeightValue').innerText = form.querySelector('#rubricWeight').value;
      loadAssignments(document.getElementById('currentCourseId').value);
    } else {
      alert('Error creating assignment');
      // Re-enable
      submitButton.disabled = false;
      submitButton.innerText = 'Create Assignment';
    }
  } catch (err) {
    alert('Network error: ' + err.message);
    submitButton.disabled = false;
    submitButton.innerText = 'Create Assignment';
  }
});