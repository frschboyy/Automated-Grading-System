document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    if (params.has('registered')) {
        const successMessage = document.getElementById('signup-success');
        if (successMessage) {
            successMessage.style.display = 'block';
        } else {
            alert('Account created successfully. Please log in.');
        }
    }
});
