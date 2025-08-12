
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
    const row = document.getElementById('row-' + qNum);

    row.querySelector('.score-text').style.display = 'none';
    row.querySelector('.maxscore-text').style.display = 'none';
    row.querySelector('.feedback-text').style.display = 'none';

    row.querySelector('.score-input').style.display = 'inline-block';
    row.querySelector('.maxscore-input').style.display = 'inline-block';
    row.querySelector('.feedback-input').style.display = 'inline-block';

    row.querySelector('.edit-btn').style.display = 'none';
    row.querySelector('.save-btn').style.display = 'inline-block';
    row.querySelector('.cancel-btn').style.display = 'inline-block';
}

function cancelEdit(qNum) {
    const row = document.getElementById('row-' + qNum);

    row.querySelector('.score-input').style.display = 'none';
    row.querySelector('.maxscore-input').style.display = 'none';
    row.querySelector('.feedback-input').style.display = 'none';

    row.querySelector('.score-text').style.display = 'inline';
    row.querySelector('.maxscore-text').style.display = 'inline';
    row.querySelector('.feedback-text').style.display = 'inline';

    row.querySelector('.edit-btn').style.display = 'inline-block';
    row.querySelector('.save-btn').style.display = 'none';
    row.querySelector('.cancel-btn').style.display = 'none';

    // Reset inputs to original values
    row.querySelector('.score-input').value = row.querySelector('.score-text').textContent.trim();
    row.querySelector('.maxscore-input').value = row.querySelector('.maxscore-text').textContent.trim();
    row.querySelector('.feedback-input').value = row.querySelector('.feedback-text').textContent.trim();
}

function saveEdit(qNum) {
    const row = document.getElementById('row-' + qNum);
    const newScore = Number(row.querySelector('.score-input').value);
    const newMaxScore = Number(row.querySelector('.maxscore-input').value);
    const newFeedback = row.querySelector('.feedback-input').value.trim();

    if (isNaN(newMaxScore) || newMaxScore < 0) {
        alert('Please enter a valid non-negative number for Max Score.');
        return;
    }

    if (isNaN(newScore) || newScore < 0 || newScore > newMaxScore) {
        alert(`Please enter a valid Score between 0 and ${newMaxScore}.`);
        return;
    }

    // Send update to server (AJAX / fetch)
    fetch(`/teacher/evaluation/update/${qNum}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({
            score: newScore,
            maxScore: newMaxScore,
            feedback: newFeedback
        })
    }).then(res => {
        if (res.ok) {
            // Update UI text spans
            row.querySelector('.score-text').textContent = newScore;
            row.querySelector('.maxscore-text').textContent = newMaxScore;
            row.querySelector('.feedback-text').textContent = newFeedback;

            // Exit edit mode
            cancelEdit(qNum);
            alert('Evaluation updated successfully');
        } else {
            alert('Error updating evaluation');
        }
    }).catch(err => {
        alert('Network error: ' + err.message);
    });
}
