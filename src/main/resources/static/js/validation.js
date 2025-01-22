const form = document.querySelector('form');
form.addEventListener('submit', (e) => {
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (password !== confirmPassword) {
        e.preventDefault();
        const errorMessage = document.getElementById('errorMessage');
        errorMessage.textContent = "Passwords do not match.";
        errorMessage.style.display = 'block';
    }
});

// Check for backend error messages on page load
window.onload = function() {
    const urlParams = new URLSearchParams(window.location.search);
    const errorMessage = urlParams.get('error');

    // If there is a backend error message in the URL, show it
    if (errorMessage) {
        const errorDiv = document.getElementById('errorMessage');
        errorDiv.textContent = decodeURIComponent(errorMessage); // Decoding the URL-encoded error message
        errorDiv.style.display = 'block';

        // Remove the error query parameter from the URL without reloading the page
        const newUrl = window.location.origin + window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);
    }
};