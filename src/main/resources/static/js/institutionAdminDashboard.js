const createTeacherForm = document.getElementById('createTeacherForm');
const assignCourseForm = document.getElementById('assignCourseForm');
const approveStudentMessage = document.getElementById('approveStudentMessage');
const createTeacherMessage = document.getElementById('createTeacherMessage');
const assignCourseMessage = document.getElementById('assignCourseMessage');

createTeacherForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    createTeacherMessage.textContent = '';
    createTeacherMessage.className = 'message';

    const formData = new FormData(createTeacherForm);
    const params = new URLSearchParams(formData);

    try {
        const res = await fetch('/institution-admin/create-teacher', {
            method: 'POST',
            body: params
        });

        const text = await res.text();

        if (res.ok) {
            createTeacherMessage.textContent = text;
            createTeacherMessage.classList.add('success');
            createTeacherForm.reset();
        } else {
            createTeacherMessage.textContent = text;
            createTeacherMessage.classList.add('error');
        }
    } catch (err) {
        createTeacherMessage.textContent = 'Error: ' + err.message;
        createTeacherMessage.classList.add('error');
    }
});

assignCourseForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    assignCourseMessage.textContent = '';
    assignCourseMessage.className = 'message';

    const formData = new FormData(assignCourseForm);
    const params = new URLSearchParams(formData);

    try {
        const res = await fetch('/institution-admin/assign-course', {
            method: 'POST',
            body: params
        });

        const text = await res.text();

        if (res.ok) {
            assignCourseMessage.textContent = text;
            assignCourseMessage.classList.add('success');
        } else {
            assignCourseMessage.textContent = text;
            assignCourseMessage.classList.add('error');
        }
    } catch (err) {
        assignCourseMessage.textContent = 'Error: ' + err.message;
        assignCourseMessage.classList.add('error');
    }
});

document.querySelectorAll('.approveBtn').forEach(button => {
    button.addEventListener('click', async () => {
        approveStudentMessage.textContent = '';
        approveStudentMessage.className = 'message';

        const studentId = button.getAttribute('data-id');

        try {
            const res = await fetch('/institution-admin/approve-student?studentId=' + studentId, {
                method: 'POST',
            });

            const text = await res.text();

            if (res.ok) {
                approveStudentMessage.textContent = text;
                approveStudentMessage.classList.add('success');
                button.closest('tr').remove();
            } else {
                approveStudentMessage.textContent = text;
                approveStudentMessage.classList.add('error');
            }
        } catch (err) {
            approveStudentMessage.textContent = 'Error: ' + err.message;
            approveStudentMessage.classList.add('error');
        }
    });
});