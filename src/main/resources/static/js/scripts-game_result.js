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

    const rows = [];
    const itemCounts = {};

    $('#purchasedItemsTable tbody tr').each(function () {
        const id = $(this).find('td:nth-child(1)').text().trim();
        const name = $(this).find('td:nth-child(2)').text().trim();
        const cost = $(this).find('td:nth-child(3)').text().trim();

        const key = `${id}_${name}_${cost}`;
        if (!itemCounts[key]) {
            itemCounts[key] = { id, name, cost, quantity: 1 };
        } else {
            itemCounts[key].quantity += 1;
        }
    });

    Object.values(itemCounts).forEach(item => {
        rows.push([item.id, item.name, item.cost, item.quantity]);
    });

    if ($.fn.DataTable.isDataTable('#purchasedItemsTable')) {
        $('#purchasedItemsTable').DataTable().destroy();
    }

    $('#purchasedItemsTable').DataTable({
        data: rows,
        columns: [
            { title: "Id" },
            { title: "Name" },
            { title: "Cost" },
            { title: "Quantity" }
        ],
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