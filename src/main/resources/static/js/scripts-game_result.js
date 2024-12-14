$(document).ready(function () {
    $('#processedMessagesTable').DataTable({
        "paging": true,
        "lengthChange": false,
        "pageLength": 10,
        "searching": false,
        "ordering": true,
        "info": true,
        "autoWidth": false,
        "responsive": true
    });

    $('#processedMessagesTable tbody').on('click', 'tr.clickable-row', function () {
        const adId = $(this).data('adid');
        const message = $(this).data('message');
        const turn = $(this).data('turn');
        const reward = $(this).data('reward');
        const success = $(this).data('success');
        const failureReason = $(this).data('failurereason');

        $('#modalAdId').text(adId);
        $('#modalMessage').text(message);
        $('#modalTurn').text(turn);
        $('#modalReward').text(reward);

        const $modalSuccess = $('#modalSuccess');

        if (success === true || success === 'true') {
            $modalSuccess
                .text('True')
                .removeClass('bg-danger')
                .addClass('bg-success');
        } else {
            $modalSuccess
                .text('False')
                .removeClass('bg-success')
                .addClass('bg-danger');
        }

        if (failureReason && failureReason.trim() !== '') {
            $('#modalFailureText').text(failureReason);
            $('#modalFailureReason').show();
        } else {
            $('#modalFailureReason').hide();
        }

        const myModal = new bootstrap.Modal(document.getElementById('messageModal'));
        myModal.show();
    });
});