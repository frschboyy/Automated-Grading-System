
document.addEventListener('DOMContentLoaded', () => {
  fetchAssignments('/api/assignments/upcoming', '.assignment-list', goToSubmission, 'Submit Assignment')
  fetchAssignments('/api/assignments/submitted', '.submission-list', goToEvaluation, 'Check Evaluation')
})

async function fetchAssignments(url, containerSelector, buttonHandler, buttonText) {
  try {
    const response = await fetch(url)
    const data = await response.json()

    const container = document.querySelector(containerSelector)
    if (!container) {
      console.error(`Error: ${containerSelector} not found.`)
      return
    }

    container.innerHTML = ''
    data.forEach(assignment => {
      const li = document.createElement('li')
      li.classList.add(containerSelector.includes('assignment') ? 'assignment-item' : 'submission-item')

      const dueDate = new Date(assignment.dueDate || '')
      li.innerHTML = `
        <div class="details">
            <div class="assignment-title">${assignment.title}</div>
            <div class="assignment-description">Description: ${assignment.description}</div>
            <div class="due-date">Due: ${dueDate.toLocaleDateString()}</div>
        </div>
        <button class="btn-primary" data-id="${assignment.id}">
            ${buttonText}
        </button>
      `

      li.querySelector('button').addEventListener('click', () => buttonHandler(assignment.id))
      container.appendChild(li)
    })
  } catch (error) {
    console.error(`Error fetching from ${url}:`, error)
  }
}

// Redirect to the submision page w/ assignment id
function goToSubmission(id) {
  window.location.href = `/submissionsPage?assignmentId=${id}`
}

// Redirect to the evaluation page w/ assignment id
function goToEvaluation(id) {
  window.location.href = `/resultsPage?assignmentId=${id}`
}