/* global alert */

// Fetch and display upcoming assignments
function fetchAllAssignments () {
  fetch('/api/assignments/all')
    .then((response) => response.json())
    .then((data) => {
      const assignmentsContainer = document.querySelector('.assignment-list')
      if (!assignmentsContainer) {
        console.error('Error: Assignment container not found.')
        return
      }

      assignmentsContainer.innerHTML = '' // Clear existing content

      // Loop through the assignments and create a list item for each one
      data.forEach((assignment) => {
        const li = document.createElement('li')
        li.classList.add('assignment-item')

        const dueDate = new Date(assignment.dueDate?.toString() || '')

          li.innerHTML = `
                  <div class="assignment-content">
                      <div class="assignment-title">${assignment.title}</div>
                      <div class="assignment-description">Description: ${assignment.description}</div>
                      <div class="due-date">Due: ${dueDate.toLocaleDateString()}</div>
                  </div>
                  <div class="assignment-actions">
                      <button class="btn-primary" 
                          data-id="${assignment.id}" 
                          data-title="${assignment.title}" 
                          data-description="${assignment.description}" 
                          data-due-date="${assignment.dueDate}">
                          View Submissions
                      </button>
                      <button class="btn-red delete-btn" data-id="${assignment.id}"> 
                          Delete Assignment
                      </button>
                  </div>
`


        // Add the event listener for each button
        const viewButton = li.querySelector('button')
        viewButton.addEventListener('click', function () {
          goToSubmission(viewButton.dataset.id, viewButton.dataset.title, viewButton.dataset.description, viewButton.dataset.dueDate)
        })
          //Add event listener for "Delete Assignment"
          const deleteButton = li.querySelector('.delete-btn')
          deleteButton.addEventListener('click', function () {
              deleteAssignment(deleteButton.dataset.id)
          })

        assignmentsContainer.appendChild(li)
      })
    })
    .catch((error) => {
      console.error('Error fetching assignments:', error)
    })
}

fetchAllAssignments()

// Redirect to the submitted assignments page
function goToSubmission (id, title, description, dueDate) {
  const assignmentData = { id, title, description, dueDate }

  fetch('/api/assignments/pushAssignmentDetails', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(assignmentData)
  })
    .then((response) => {
      if (response.ok) {
        window.location.href = '/submissions-page'
      } else {
        throw new Error('Failed to push assignment details.')
      }
    })
    .catch((error) => {
      console.error('Error:', error)
      alert('An error occurred while saving assignment details.')
    })
}

// Delete assignment function
function deleteAssignment(id) {
    // Confirm before deleting
    if (confirm('Are you sure you want to delete this assignment?')) {
        fetch(`/api/assignments/delete/${id}`, {
            method: 'DELETE'
        })
            .then((response) => {
                if (response.ok) {
                    // Refresh the assignments list after successful deletion
                    fetchAllAssignments()
                    alert('Assignment deleted successfully!')
                } else {
                    throw new Error('Failed to delete assignment.')
                }
            })
            .catch((error) => {
                console.error('Error deleting assignment:', error)
                alert('An error occurred while deleting the assignment.')
            })
    }
}
