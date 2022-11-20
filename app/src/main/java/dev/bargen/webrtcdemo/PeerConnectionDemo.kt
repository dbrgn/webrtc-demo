package dev.bargen.webrtcdemo

import android.content.Context
import android.util.Log
import org.webrtc.*

const val TAG = "PeerConnectionDemo"

var initialized = false

// List of STUN servers that are publicly accessible without credentials
val stunServers = listOf(
    // Google
    PeerConnection.IceServer.builder(listOf("stun:stun1.l.google.com:19302")).createIceServer(),
    // Nextcloud
    PeerConnection.IceServer.builder(listOf("stun:stun.nextcloud.com:443")).createIceServer(),
)

class PeerConnectionDemo(context: Context, rootEglBase: EglBase, private val onEvent: (String) -> Unit) {
    private val peerConnectionFactory: PeerConnectionFactory
    private val peerConnection: PeerConnection

    init {
        // Initialize WebRTC if necessary
        if (!initialized) {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions
                    .builder(context)
                    .setEnableInternalTracer(true)
                    .createInitializationOptions()
            )
            initialized = true
        }

        // Create factory
        Log.i(TAG, "Creating peer connection factory")
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true)
            )
            .createPeerConnectionFactory()

        // Create peer connection
        val rtcConfig = PeerConnection.RTCConfiguration(stunServers)
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_ONCE
        Log.i(TAG, "Creating peer connection")
        peerConnection = peerConnectionFactory
            .createPeerConnection(rtcConfig, PeerConnectionObserver(onEvent))
            ?: throw Error("Could not initialize peer connection (builder returned null)")
        Log.i(TAG, "Created peer connection")
    }

    fun gatherCandidates() {
        // Create offer to trigger ICE collection
        val mediaConstraints = MediaConstraints()
        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        peerConnection.createOffer(SdpObserver(peerConnection, onEvent), mediaConstraints)
    }

    /**
     * Clean up. Do not use the class instance after this call anymore!
     */
    fun dispose() {
        peerConnectionFactory.dispose()
    }
}

class PeerConnectionObserver(private val onEvent: (String) -> Unit) : PeerConnection.Observer {
    @Suppress("PrivatePropertyName")
    private val TAG = "PeerConnectionObserver"

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.i(TAG, "onSignalingChange")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.i(TAG, "onIceConnectionChange")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.i(TAG, "onIceConnectionReceivingChange")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Log.i(TAG, "onIceGatheringChange")
    }

    override fun onIceCandidate(candidate: IceCandidate?) {
        Log.i(TAG, "onIceCandidate")
        candidate?.apply {
            onEvent("New ICE candidate: " + this.sdp)
        }
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Log.i(TAG, "onIceCandidatesRemoved")
    }

    override fun onAddStream(p0: MediaStream?) {
        Log.i(TAG, "onAddStream")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        Log.i(TAG, "onRemoveStream")
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.i(TAG, "onDataChannel")
    }

    override fun onRenegotiationNeeded() {
        Log.i(TAG, "onRenegotiationNeeded")
    }
}

class SdpObserver(
    private val peerConnection: PeerConnection,
    private val onEvent: (String) -> Unit,
) : org.webrtc.SdpObserver {
    @Suppress("PrivatePropertyName")
    private val TAG = "SdpObserver"

    override fun onCreateSuccess(description: SessionDescription?) {
        Log.i(TAG, "onCreateSuccess")
        description?.apply {
            onEvent("Local SDP created")
            peerConnection.setLocalDescription(this@SdpObserver, this)
        }
    }

    override fun onSetSuccess() {
        Log.i(TAG, "onSetSuccess")
        onEvent("Local description set")
    }

    override fun onCreateFailure(p0: String?) {
        Log.i(TAG, "onCreateFailure")
    }

    override fun onSetFailure(p0: String?) {
        Log.i(TAG, "onSetFailure")
    }

}