document.getElementById('backToDashboard').addEventListener('click', e => {
    e.preventDefault();
    const courseId = sessionStorage.getItem('currentCourseId');
    if (courseId) {
        window.location.href = `/dashboard?courseId=${courseId}`;
    } else {
        window.location.href = '/dashboard';
    }
});
