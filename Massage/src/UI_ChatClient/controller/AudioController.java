package UI_ChatClient.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;

/**
 * Controller xử lý ghi âm và phát âm thanh với pause/resume
 */
public class AudioController {
    
    private boolean isRecording = false;
    private TargetDataLine audioLine;
    private ByteArrayOutputStream recordOutputStream;
    private Thread recordingThread;
    
    private final AudioFormat format = new AudioFormat(16000, 8, 1, true, true);
    
    // Playback control
    private Clip currentClip;
    private boolean isPlaying = false;
    private File currentPlayingFile;
    private List<PlaybackListener> playbackListeners = new ArrayList<>();
    private Thread playbackMonitorThread;
    private volatile boolean monitorRunning = false;
    
    public interface RecordingCallback {
        void onRecordingStarted();
        void onRecordingStopped(File audioFile);
        void onRecordingError(String message);
    }
    
    public interface PlaybackListener {
        void onPlaybackStarted(File audioFile);
        void onPlaybackPaused(File audioFile);
        void onPlaybackResumed(File audioFile);
        void onPlaybackStopped(File audioFile);
    }
    
    private RecordingCallback callback;
    
    public void setCallback(RecordingCallback callback) {
        this.callback = callback;
    }
    
    public void addPlaybackListener(PlaybackListener listener) {
        playbackListeners.add(listener);
    }
    
    public void removePlaybackListener(PlaybackListener listener) {
        playbackListeners.remove(listener);
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public boolean isPlayingFile(File file) {
        return isPlaying && currentPlayingFile != null && currentPlayingFile.equals(file);
    }
    
    public File getCurrentPlayingFile() {
        return currentPlayingFile;
    }
    
    public void startRecording() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                if (callback != null) callback.onRecordingError("Micro không được hỗ trợ.");
                return;
            }
            audioLine = (TargetDataLine) AudioSystem.getLine(info);
            audioLine.open(format);
            audioLine.start();
            
            recordOutputStream = new ByteArrayOutputStream();
            isRecording = true;
            
            recordingThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[4096];
                    
                    while (isRecording) {
                        int bytesRead = audioLine.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            recordOutputStream.write(buffer, 0, bytesRead);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Luồng ghi âm đã dừng: " + e.getMessage());
                }
            });
            recordingThread.start();
            
            if (callback != null) callback.onRecordingStarted();
            
        } catch (LineUnavailableException e) {
            if (callback != null) callback.onRecordingError("Lỗi micro: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stopRecordingAndSend() {
        if (!isRecording) return;
        isRecording = false;
        audioLine.stop();
        audioLine.close();
        
        try {
            byte[] audioData = recordOutputStream.toByteArray();
            File audioFile = File.createTempFile("voice_message_", ".wav");
            audioFile.deleteOnExit();
            
            try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                 AudioInputStream ais = new AudioInputStream(bais, format, audioData.length / format.getFrameSize())) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, audioFile);
            }
            
            if (callback != null) callback.onRecordingStopped(audioFile);
            
        } catch (IOException e) {
            if (callback != null) callback.onRecordingError("Lỗi khi lưu tin nhắn thoại: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Toggle play/pause for audio file
     */
    public void togglePlayPause(File audioFile) {
        if (currentClip != null && currentPlayingFile != null && currentPlayingFile.equals(audioFile)) {
            // Same file - toggle pause/resume
            if (isPlaying) {
                pauseAudio();
            } else {
                // Check if clip has finished
                if (currentClip.getFramePosition() >= currentClip.getFrameLength()) {
                    // Clip finished, restart from beginning
                    currentClip.setFramePosition(0);
                }
                resumeAudio();
            }
        } else {
            // Different file or no file playing - start new playback
            stopAudio();
            playAudio(audioFile);
        }
    }
    
    public void playAudio(File audioFile) {
        // Stop any currently playing audio
        stopAudio();
        
        new Thread(() -> {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                currentClip = AudioSystem.getClip();
                currentClip.open(audioInputStream);
                currentPlayingFile = audioFile;
                
                currentClip.start();
                isPlaying = true;
                notifyPlaybackStarted(audioFile);
                
                // Start monitor thread to detect when playback finishes
                startPlaybackMonitor(audioFile);
                
            } catch (Exception e) {
                System.err.println("Không thể phát âm thanh: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    private void startPlaybackMonitor(File audioFile) {
        monitorRunning = true;
        playbackMonitorThread = new Thread(() -> {
            try {
                while (monitorRunning && currentClip != null) {
                    if (isPlaying && currentClip.getFramePosition() >= currentClip.getFrameLength()) {
                        // Playback finished naturally
                        isPlaying = false;
                        monitorRunning = false;
                        notifyPlaybackStopped(audioFile);
                        break;
                    }
                    Thread.sleep(100); // Check every 100ms
                }
            } catch (InterruptedException e) {
                // Thread interrupted, exit gracefully
            }
        });
        playbackMonitorThread.start();
    }
    
    public void pauseAudio() {
        if (currentClip != null && isPlaying) {
            if (playbackMonitorThread != null) {
                playbackMonitorThread.interrupt();
                playbackMonitorThread = null;
            }
            currentClip.stop();
            isPlaying = false;
            notifyPlaybackPaused(currentPlayingFile);
        }
    }
    
    public void resumeAudio() {
        if (currentClip != null && !isPlaying) {
            currentClip.start();
            isPlaying = true;
            notifyPlaybackResumed(currentPlayingFile);
            startPlaybackMonitor(currentPlayingFile);
        }
    }
    
    public void stopAudio() {
        monitorRunning = false;
        if (playbackMonitorThread != null) {
            playbackMonitorThread.interrupt();
            playbackMonitorThread = null;
        }
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            File stoppedFile = currentPlayingFile;
            currentClip = null;
            currentPlayingFile = null;
            isPlaying = false;
            if (stoppedFile != null) {
                notifyPlaybackStopped(stoppedFile);
            }
        }
    }
    
    public long getClipPosition() {
        if (currentClip != null) {
            return currentClip.getMicrosecondPosition();
        }
        return 0;
    }
    
    public long getClipLength() {
        if (currentClip != null) {
            return currentClip.getMicrosecondLength();
        }
        return 0;
    }
    
    public int getRemainingSeconds() {
        if (currentClip != null) {
            long remainingMicros = currentClip.getMicrosecondLength() - currentClip.getMicrosecondPosition();
            return (int)(remainingMicros / 1_000_000);
        }
        return 0;
    }
    
    private void notifyPlaybackStarted(File file) {
        for (PlaybackListener listener : playbackListeners) {
            listener.onPlaybackStarted(file);
        }
    }
    
    private void notifyPlaybackPaused(File file) {
        for (PlaybackListener listener : playbackListeners) {
            listener.onPlaybackPaused(file);
        }
    }
    
    private void notifyPlaybackResumed(File file) {
        for (PlaybackListener listener : playbackListeners) {
            listener.onPlaybackResumed(file);
        }
    }
    
    private void notifyPlaybackStopped(File file) {
        for (PlaybackListener listener : playbackListeners) {
            listener.onPlaybackStopped(file);
        }
    }
}
