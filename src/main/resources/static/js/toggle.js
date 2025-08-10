document.addEventListener("DOMContentLoaded", function () {
    const signupForm = document.getElementById("signup-form");
    const signupCard = document.getElementById("signup-card");
    const otpSection = document.getElementById("otp-section");
    const otpButton = document.getElementById("verify-otp-btn");
    const otpError = document.getElementById("otp-error");

    signupForm.addEventListener("submit", function (e) {
        e.preventDefault(); // Stop normal form submission

        fetch("/signup", {
            method: "POST",
            body: new FormData(signupForm)
        })
            .then(res => res.json())
            .then(data => {
                if (data.requireOtp) {
                    signupCard.style.display = "none";
                    otpSection.style.display = "block";
                } else if (data.success) {
                    window.location.href = "/dashboard";
                } else {
                    document.getElementById("error-message").style.display = "block";
                }
            });
    });

    otpButton.addEventListener("click", function () {
        const otp = document.getElementById("otp-code").value;

        fetch("/verify-otp", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: "otp=" + encodeURIComponent(otp)
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    window.location.href = "/dashboard";
                } else {
                    otpError.style.display = "block";
                }
            });
    });
});