document.addEventListener('DOMContentLoaded', () => {
    const signupForm = document.getElementById('signup-form');
    const otpSection = document.getElementById('otp-section');
    const otpInput = document.getElementById('otp-code');
    const verifyBtn = document.getElementById('verify-otp-btn');
    const otpError = document.getElementById('otp-error');

    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        // --- Validation logic here ---
        const firstName = document.getElementById('firstName').value.trim();
        const lastName = document.getElementById('lastName').value.trim();
        const institution = document.getElementById('institutionId').value;
        const registrationId = document.getElementById('registrationId').value;
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const errorMessage = document.getElementById('error-message');

        if (!firstName || !lastName || !institution || !email || !password || !registrationId || password !== confirmPassword) {
            errorMessage.textContent =
                !firstName ? 'First name is required.' :
                    !lastName ? 'Last name is required.' :
                        !institution ? 'Please select your institution.' :
                            !registrationId ? 'Please include your registration ID.' :
                                !email ? 'Email is required.' :
                                    !password ? 'Password is required.' :
                                        'Passwords do not match.';
            errorMessage.style.display = 'block';
            return; // stops OTP sending
        }
        errorMessage.style.display = 'none';

        // Disable the button to prevent multiple submissions
        const signupBtn = signupForm.querySelector('button[type="submit"]');
        signupBtn.disabled = true;
        signupBtn.textContent = 'Signing up...';

        try {
            // If validation is successful
            const formData = new FormData(signupForm);
            const params = new URLSearchParams(formData);

            const res = await fetch('/signup', {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: params
            });

            const result = await res.json();

            if (result.success && result.requireOtp) {
                signupForm.style.display = 'none';
                otpSection.style.display = 'block';
                alert(result.message);
            } else if (result.success) {
                window.location.href = result.redirect;
            } else {
                alert(result.message || 'Signup failed');
                signupBtn.disabled = false;
                signupBtn.textContent = 'Sign Up';
            }
        } catch (err) {
            alert('Error: ' + err.message);
            signupBtn.disabled = false;
            signupBtn.textContent = 'Sign Up';
        }
    });

    verifyBtn.addEventListener('click', async () => {
        const email = signupForm.querySelector('input[name="email"]').value;
        const otp = otpInput.value.trim();

        if (!otp) {
            otpError.textContent = 'Please enter the OTP';
            otpError.style.display = 'block';
            return;
        }

        try {
            const res = await fetch('/verify-otp', {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({ email, otp })
            });
            const result = await res.json();

            if (result.success) {
                if (result.redirect) {
                    window.location.href = '/login?registered=true';
                } else if (result.pendingApproval) {
                    alert(result.message);
                }
            } else {
                otpError.textContent = result.message || 'Invalid OTP';
                otpError.style.display = 'block';
            }
        } catch (err) {
            otpError.textContent = 'Error: ' + err.message;
            otpError.style.display = 'block';
        }
    });
});
