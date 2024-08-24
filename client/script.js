$(document).ready(function () {
    function showMessage(message, type, target) {
        const messageBox = $(target);
        messageBox.removeClass('success error');
        messageBox.addClass(type);
        messageBox.text(message);
        messageBox.show();
        setTimeout(function() {
            messageBox.fadeOut();
        }, 5000); // Сообщение исчезает через 5 секунд
    }

    // Добавление студента
    $('#addStudentForm').submit(function (event) {
        event.preventDefault();
        const studentData = {
            first_name: $('#firstName').val(),
            last_name: $('#lastName').val(),
            middle_name: $('#middleName').val(),
            birth_date: $('#birthDate').val(),
            student_group: $('#studentGroup').val()
        };

        $.ajax({
            url: 'http://localhost:4567/students',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(studentData),
            success: function (response) {
                showMessage(response, 'success', '#addMessage');
                $('#addStudentForm')[0].reset();
            },
            error: function () {
                showMessage('Error adding student', 'error', '#addMessage');
            }
        });
    });

    // Удаление студента
    $('#deleteStudentForm').submit(function (event) {
        event.preventDefault();
        const studentId = $('#studentId').val();

        $.ajax({
            url: 'http://localhost:4567/students/' + studentId,
            type: 'DELETE',
            success: function (response) {
                showMessage(response, 'success', '#deleteMessage');
                $('#deleteStudentForm')[0].reset();
            },
            error: function () {
                showMessage('Error deleting student', 'error', '#deleteMessage');
            }
        });
    });

    // Получение списка студентов
    $('#listStudents').click(function () {
        $.ajax({
            url: 'http://localhost:4567/students',
            type: 'GET',
            success: function (response) {
                const students = response.split("\n").filter(line => line.trim() !== "");
                const tbody = $('#studentList tbody');
                tbody.empty(); // Очистить текущий список студентов

                students.forEach(student => {
                    const data = student.split(", ");
                    const row = $('<tr></tr>');
                    data.forEach(field => {
                        const cell = $('<td></td>').text(field.split(": ")[1]);
                        row.append(cell);
                    });
                    tbody.append(row);
                });
                showMessage('Student list updated', 'success', '#listMessage');
            },
            error: function () {
                showMessage('Error fetching students', 'error', '#listMessage');
            }
        });
    });
});
