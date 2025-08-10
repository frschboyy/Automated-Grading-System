document.addEventListener('DOMContentLoaded', () => {
  const fileInput = document.getElementById('file-input');
  const fileList = document.getElementById('file-list');
  const uploadZone = document.querySelector('.upload-zone');
  const submitBtn = document.getElementById('submit-btn');
  const loader = document.getElementById('loader');
  const allowedExtensions = ['pdf', 'doc', 'docx'];

  function handleFileUpload(event) {
    fileList.innerHTML = '';
    Array.from(event.target.files).forEach((file) => {
      const fileExtension = file.name.split('.').pop().toLowerCase();
      if (!allowedExtensions.includes(fileExtension)) {
        alert(`File type not allowed: ${file.name}`);
        return;
      }
      const fileItem = document.createElement('div');
      fileItem.classList.add('file-item');
      fileItem.innerHTML = `
                <div class="file-info">
                    <span>📄</span><span>${file.name}</span>
                    <span>(${(file.size / (1024 * 1024)).toFixed(2)} MB)</span>
                </div>
            `;
      const removeButton = document.createElement('button');
      removeButton.className = 'btn btn-secondary';
      removeButton.textContent = 'Remove';
      removeButton.addEventListener('click', () => {
        fileItem.remove();
        const dataTransfer = new DataTransfer();
        Array.from(fileInput.files)
          .filter(f => f !== file)
          .forEach(f => dataTransfer.items.add(f));
        fileInput.files = dataTransfer.files;
      });
      fileItem.appendChild(removeButton);
      fileList.appendChild(fileItem);
    });
  }

  function submitFiles() {
    if (fileInput.files.length === 0) {
      alert('Please upload at least one file before submitting.');
      return;
    }

    submitBtn.style.display = 'none';
    loader.style.display = 'block';

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    fetch('/api/submissions', { method: 'POST', body: formData })
      .then(res => res.ok ? res.json() :
        res.json().then(data => { throw new Error(data.error || 'Failed to upload file.'); }))
      .then(data => {
        loader.style.display = 'none';
        alert(`Success: ${data.message || 'Uploaded successfully'}`);
        window.location.href = '/dashboard';
      })
      .catch(error => {
        loader.style.display = 'none';
        submitBtn.style.display = 'inline';
        alert(`Error: ${error.message}`);
      });
  }

  if (uploadZone) uploadZone.addEventListener('click', () => fileInput.click());
  if (fileInput) fileInput.addEventListener('change', handleFileUpload);
  if (submitBtn) submitBtn.addEventListener('click', submitFiles);
});
