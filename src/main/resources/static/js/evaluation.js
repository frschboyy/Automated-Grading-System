
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

// Editing functions
function startEdit(qNum) {
    const modal = document.getElementById('modal-' + qNum);

    modal.querySelector('.score-text').style.display = 'none';
    modal.querySelector('.maxscore-text').style.display = 'none';
    modal.querySelector('.feedback-text').style.display = 'none';

    modal.querySelector('.score-input').style.display = 'inline-block';
    modal.querySelector('.maxscore-input').style.display = 'inline-block';
    modal.querySelector('.feedback-input').style.display = 'inline-block';

    modal.querySelector('.edit-btn').style.display = 'none';
    modal.querySelector('.save-btn').style.display = 'inline-block';
    modal.querySelector('.cancel-btn').style.display = 'inline-block';
}

function cancelEdit(qNum) {
    const modal = document.getElementById('modal-' + qNum);

    modal.querySelector('.score-input').style.display = 'none';
    modal.querySelector('.maxscore-input').style.display = 'none';
    modal.querySelector('.feedback-input').style.display = 'none';

    modal.querySelector('.score-text').style.display = 'inline';
    modal.querySelector('.maxscore-text').style.display = 'inline';
    modal.querySelector('.feedback-text').style.display = 'inline';

    modal.querySelector('.edit-btn').style.display = 'inline-block';
    modal.querySelector('.save-btn').style.display = 'none';
    modal.querySelector('.cancel-btn').style.display = 'none';

    // Reset values
    modal.querySelector('.score-input').value = modal.querySelector('.score-text').textContent.trim();
    modal.querySelector('.maxscore-input').value = modal.querySelector('.maxscore-text').textContent.trim();
    modal.querySelector('.feedback-input').value = modal.querySelector('.feedback-text').textContent.trim();
}

function saveEdit(qNum) {
    const modal = document.getElementById('modal-' + qNum);
    const newScore = Number(modal.querySelector('.score-input').value);
    const newMaxScore = Number(modal.querySelector('.maxscore-input').value);
    const newFeedback = modal.querySelector('.feedback-input').value.trim();

    if (isNaN(newMaxScore) || newMaxScore < 0) {
        alert('Please enter a valid non-negative number for Max Score.');
        return;
    }
    if (isNaN(newScore) || newScore < 0 || newScore > newMaxScore) {
        alert(`Please enter a valid Score between 0 and ${newMaxScore}.`);
        return;
    }

    fetch(`/teacher/evaluation/update/${qNum}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({ score: newScore, maxScore: newMaxScore, feedback: newFeedback })
    }).then(res => {
        if (res.ok) {
            // Format numbers with 1 decimal place
            const displayScore = newScore.toFixed(1);
            const displayMaxScore = newMaxScore.toFixed(1);

            // Update modal text
            modal.querySelector('.score-text').textContent = displayScore;
            modal.querySelector('.maxscore-text').textContent = displayMaxScore;
            modal.querySelector('.feedback-text').textContent = newFeedback;

            // Also update table row for consistency
            const row = document.getElementById('row-' + qNum);
            if (row) {
                row.querySelector('.score-cell').textContent = displayScore;
                row.querySelector('.maxscore-cell').textContent = displayMaxScore;
                row.querySelector('.feedback-cell').textContent = newFeedback;
            }

            cancelEdit(qNum);
            alert('Evaluation updated successfully');
        } else {
            alert('Error updating evaluation');
        }
    }).catch(err => alert('Network error: ' + err.message));
}
