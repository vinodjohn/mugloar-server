$(document).ready(function () {
    $('#gameHistoryTable').DataTable({
        "paging": true,
        "lengthChange": false,
        "pageLength": 10,
        "searching": false,
        "ordering": true,
        "info": true,
        "autoWidth": false,
        "responsive": true
    });

    $('#gameHistoryTable tbody').on('click', 'tr.clickable-row', function () {
        const gameId = $(this).data('gameid');

        if (gameId) {
            window.location.href = '/game/' + gameId;
        }
    });
});