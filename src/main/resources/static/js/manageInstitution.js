document.addEventListener("DOMContentLoaded", () => {
    fetch("/api/institutions-data")
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById("institutionList");
            tableBody.innerHTML = "";

            data.forEach(inst => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${inst.name}</td>
                    <td>${inst.email || "—"}</td>
                    <td>${inst.emailDomain || "—"}</td>
                    <td>${inst.inviteCode || "—"}</td>
                    <td>${inst.verificationMode}</td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(err => console.error("Error loading institutions:", err));
});
