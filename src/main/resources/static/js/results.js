function showModal(id) {
    const modal = document.getElementById('modal-' + id);
    if (modal) modal.style.display = 'block';
}

function closeModal(el) {
    el.closest('.modal').style.display = 'none';
}

window.onclick = function (event) {
    if (event.target.classList.contains('modal')) {
        event.target.style.display = 'none';
    }
}