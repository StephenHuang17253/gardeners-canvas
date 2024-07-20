$(document).ready(function() {
    $('#exampleModal').on('shown.bs.modal', function () {
        $('#myInput').trigger('focus');
    });
});