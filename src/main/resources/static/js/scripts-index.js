document.addEventListener('DOMContentLoaded', function () {
    const startGameBtn = document.getElementById('startGameBtn');
    const gameStatusContainer = document.getElementById('gameStatusContainer');
    const statusMessage = document.getElementById('statusMessage');
    const statusSpinner = document.getElementById('statusSpinner');
    let stompClient = null;
    let gameId = null;

    startGameBtn.addEventListener('click', function () {
        startGameBtn.disabled = true;
        startGameBtn.classList.add('btn-secondary');
        startGameBtn.textContent = 'Game Started!';

        gameStatusContainer.style.display = 'block';

        showSpinner();
        updateStatus('Starting the game...');

        // Start a new game by sending a POST request to /game/start
        fetch('/game/start', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({})
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                gameId = data.gameId;
                updateStatus('Game ID: ' + gameId);
                connectWebSocket(gameId);
            })
            .catch(error => {
                console.error('Error starting the game:', error);
                updateStatus('Error starting the game.');
                hideSpinner();
                startGameBtn.disabled = false;
                startGameBtn.classList.remove('btn-secondary');
                startGameBtn.textContent = 'Start New Game';
            });
    });

    /**
     * Establishes a WebSocket connection and subscribes to game status updates.
     *
     * @param {string} gameId - The unique identifier for the game session.
     */
    function connectWebSocket(gameId) {
        const socket = new SockJS('/game-websocket');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/game-status/' + gameId, function (messageOutput) {
                const gameState = JSON.parse(messageOutput.body);
                handleGameState(gameState);
            });

            stompClient.send("/app/game-start", {}, JSON.stringify({'gameId': gameId}));
        }, function (error) {
            console.error('WebSocket connection error:', error);
            updateStatus('WebSocket connection error.');
            hideSpinner();
            startGameBtn.disabled = false;
            startGameBtn.classList.remove('btn-secondary');
            startGameBtn.textContent = 'Start New Game';
        });
    }

    /**
     * Handles incoming game state messages from the backend.
     *
     * @param {Object} gameState - The game state data received from the backend.
     */
    function handleGameState(gameState) {
        if (!gameState || !gameState.state) {
            updateStatus('Received invalid game state.');
            hideSpinner();
            return;
        }

        switch (gameState.state) {
            case 'game_initialized':
                updateStatus('Game initialized.');
                hideSpinner();
                break;
            case 'investigation_failed':
                updateStatus('Investigation failed.');
                hideSpinner();
                break;
            case 'investigation_completed':
                updateStatus('Investigation phase completed.');
                hideSpinner();
                break;
            case 'now_solving_message':
                updateStatus('Now solving message: ' + gameState.message);
                showSpinner();
                break;
            case 'message_solved':
                updateStatus('Successfully solved message: ' + gameState.message);
                hideSpinner();
                break;
            case 'message_failed':
                updateStatus('Failed to solve message: ' + gameState.message);
                hideSpinner();
                break;
            case 'now_purchasing_item':
                updateStatus('Now purchasing item: ' + gameState.message);
                showSpinner();
                break;
            case 'item_purchased':
                updateStatus('Successfully purchased item: ' + gameState.message);
                hideSpinner();
                break;
            case 'item_purchase_failed':
                updateStatus('Failed to purchase item: ' + gameState.message);
                hideSpinner();
                break;
            case 'shop_phase_completed':
                updateStatus('Shop phase completed.');
                hideSpinner();
                break;
            case 'game_over':
                updateStatus('Game Over. Redirecting to results...');
                showSpinner();

                if (stompClient) {
                    stompClient.disconnect(function () {
                        console.log('Disconnected from WebSocket.');
                    });
                }

                setTimeout(() => {
                    window.location.href = `/game/${gameId}`;
                }, 3000);
                break;
            case 'game_completed':
                updateStatus('Game completed successfully! Redirecting to results...');
                showSpinner();

                if (stompClient) {
                    stompClient.disconnect(function () {
                        console.log('Disconnected from WebSocket.');
                    });
                }

                setTimeout(() => {
                    window.location.href = `/game/${gameId}`;
                }, 3000);
                break;
            case 'no_messages_available':
                updateStatus('No messages available to solve.');
                hideSpinner();
                break;
            case 'no_suitable_messages':
                updateStatus('No suitable messages left to solve.');
                hideSpinner();
                break;
            case 'missing_items':
                updateStatus('Missing items for message. Attempting to purchase required items.');
                showSpinner();
                break;
            case 'shop_item_not_found':
                updateStatus('Required shop item not found.');
                hideSpinner();
                break;
            case 'no_missing_items':
                updateStatus('No missing items to purchase.');
                hideSpinner();
                break;
            case 'shop_error':
                updateStatus('Encountered an error during shopping phase.');
                hideSpinner();
                break;
            case 'shop_unexpected_error':
                updateStatus('Error occurred while purchasing item.');
                hideSpinner();
                break;
            case 'purchase_response_null':
                updateStatus('Purchase response is null.');
                hideSpinner();
                break;
            case 'unknown_purchase_effect':
                updateStatus('Purchase message is null or empty.');
                hideSpinner();
                break;
            case 'item_effect_applied':
                updateStatus(gameState.message);
                hideSpinner();
                break;
            case 'game_result_exists':
                updateStatus('Game result already exists. Skipping save.');
                hideSpinner();
                break;
            case 'game_result_saved':
                updateStatus('Game result saved successfully.');
                hideSpinner();
                break;
            case 'duplicate_game_result':
                updateStatus('Duplicate game result detected.');
                hideSpinner();
                break;
            case 'game_result_save_failed':
                updateStatus('Failed to save game result.');
                hideSpinner();
                break;
            case 'unknown_item_effect':
                updateStatus('Unknown item effect: ' + gameState.message);
                hideSpinner();
                break;
            default:
                updateStatus(`Unknown state: ${gameState.state}`);
                hideSpinner();
        }
    }

    /**
     * Updates the status message in the UI with a fade effect.
     *
     * @param {string} message - The status message to display.
     */
    function updateStatus(message) {
        statusMessage.style.opacity = '0';
        setTimeout(() => {
            statusMessage.textContent = message;
            statusMessage.style.opacity = '1';
        }, 500);
    }

    /**
     * Shows the spinner next to the status message.
     */
    function showSpinner() {
        statusSpinner.style.display = 'inline-block';
    }

    /**
     * Hides the spinner.
     */
    function hideSpinner() {
        statusSpinner.style.display = 'none';
    }
});