package com.telus.noteapp.service;

import com.telus.noteapp.dao.NotesRepository;
import com.telus.noteapp.modal.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class NotesServiceTest {

    @Mock
    private NotesRepository notesRepository;

    private NotesService notesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notesService = new NotesService(notesRepository);
    }

    @Test
    void testAddNote() {
        Note note = new Note();
        note.setSubject("Test Subject");
        note.setDescription("Test Description");

        when(notesRepository.save(any(Note.class))).thenReturn(note);

        Note createdNote = notesService.addNote(note);

        assertNotNull(createdNote);
        assertEquals("Test Subject", createdNote.getSubject());
        verify(notesRepository, times(1)).save(any(Note.class));
    }

    @Test
    void testModifyNote() {
        Long noteId = 1L;
        Note existingNote = new Note();
        existingNote.setNoteId(noteId);
        existingNote.setSubject("Old Subject");
        existingNote.setDescription("Old Description");

        Note updatedNoteDetails = new Note();
        updatedNoteDetails.setSubject("Updated Subject");
        updatedNoteDetails.setDescription("Updated Description");

        when(notesRepository.findById(noteId)).thenReturn(Optional.of(existingNote));
        when(notesRepository.save(any(Note.class))).thenReturn(updatedNoteDetails);

        Note modifiedNote = notesService.modifyNote(noteId, updatedNoteDetails);

        assertNotNull(modifiedNote);
        assertEquals("Updated Subject", modifiedNote.getSubject());
        assertEquals("Updated Description", modifiedNote.getDescription());
        verify(notesRepository, times(1)).findById(noteId);
        verify(notesRepository, times(1)).save(any(Note.class));
    }

    @Test
    void testDeleteNote() {
        Long noteId = 1L;
        Note note = new Note();
        note.setNoteId(noteId);

        when(notesRepository.findById(noteId)).thenReturn(Optional.of(note));

        notesService.deleteNote(noteId);

        verify(notesRepository, times(1)).findById(noteId);
        verify(notesRepository, times(1)).delete(note);
    }

    @Test
    void testDeleteNote_NotFound() {
        Long noteId = 1L;

        when(notesRepository.findById(noteId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> notesService.deleteNote(noteId));
    }

    @Test
    void testSearchNotesBySubject() {
        String subject = "Test Subject";
        Note note = new Note();
        note.setSubject(subject);
        when(notesRepository.findBySubjectContainingIgnoreCase(subject)).thenReturn(List.of(note));

        List<Note> foundNotes = notesService.searchNotesBySubject(subject);

        assertNotNull(foundNotes);
        assertEquals(1, foundNotes.size());
        assertEquals(subject, foundNotes.get(0).getSubject());
        verify(notesRepository, times(1)).findBySubjectContainingIgnoreCase(subject);
    }

    @Test
    void testGetNoteById() {
        Long noteId = 1L;
        Note note = new Note();
        note.setNoteId(noteId);

        when(notesRepository.findById(noteId)).thenReturn(Optional.of(note));

        Note foundNote = notesService.getNoteById(noteId);

        assertNotNull(foundNote);
        assertEquals(noteId, foundNote.getNoteId());
        verify(notesRepository, times(1)).findById(noteId);
    }

    @Test
    void testGetNoteById_NotFound() {
        Long noteId = 1L;

        when(notesRepository.findById(noteId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> notesService.getNoteById(noteId));
    }

    @Test
    void testCountTotalNotes() {
        when(notesRepository.count()).thenReturn(5L);

        Long totalNotes = notesService.countTotalNotes();

        assertEquals(5L, totalNotes);
        verify(notesRepository, times(1)).count();
    }

    @Test
    void testGetWordCount() {
        Long noteId = 1L;
        Note note = new Note();
        note.setNoteId(noteId);
        note.setDescription("This is a test note.");

        when(notesRepository.findById(noteId)).thenReturn(Optional.of(note));

        Integer wordCount = notesService.getWordCount(noteId);

        assertEquals(5, wordCount);
        verify(notesRepository, times(1)).findById(noteId);
    }

    @Test
    void testGetAverageNoteLength() {
        Note note1 = new Note();
        note1.setDescription("This is a test note.");
        Note note2 = new Note();
        note2.setDescription("This is another test note.");

        when(notesRepository.findAll()).thenReturn(List.of(note1, note2));

        Double averageLength = notesService.getAverageNoteLength();

        assertEquals(5.0, averageLength);
        verify(notesRepository, times(1)).findAll();
    }

    @Test
    void testLikeNote() {
        Long noteId = 1L;
        Note note = new Note();
        note.setNoteId(noteId);
        note.setLikes(0);

        when(notesRepository.findById(noteId)).thenReturn(Optional.of(note));
        when(notesRepository.save(any(Note.class))).thenReturn(note);

        Note likedNote = notesService.likeNote(noteId);

        assertNotNull(likedNote);
        assertEquals(1, likedNote.getLikes());
        verify(notesRepository, times(1)).findById(noteId);
        verify(notesRepository, times(1)).save(any(Note.class));
    }

    @Test
    void testUnlikeNote() {
        Long noteId = 1L;
        Note note = new Note();
        note.setNoteId(noteId);
        note.setLikes(1);

        when(notesRepository.findById(noteId)).thenReturn(Optional.of(note));
        when(notesRepository.save(any(Note.class))).thenReturn(note);

        Note unlikedNote = notesService.unlikeNote(noteId);

        assertNotNull(unlikedNote);
        assertEquals(0, unlikedNote.getLikes());
        verify(notesRepository, times(1)).findById(noteId);
        verify(notesRepository, times(1)).save(any(Note.class));
    }

    @Test
    void testGetLikedNotes() {
        Note likedNote = new Note();
        likedNote.setLikes(5);

        when(notesRepository.findByLikesGreaterThan(0)).thenReturn(List.of(likedNote));

        List<Note> likedNotes = notesService.getLikedNotes();

        assertNotNull(likedNotes);
        assertEquals(1, likedNotes.size());
        assertEquals(5, likedNotes.get(0).getLikes());
        verify(notesRepository, times(1)).findByLikesGreaterThan(0);
    }

    @Test
    void testGetAllNotes() {
        Note note1 = new Note();
        Note note2 = new Note();

        when(notesRepository.findAll()).thenReturn(List.of(note1, note2));

        List<Note> allNotes = notesService.getAllNotes();

        assertNotNull(allNotes);
        assertEquals(2, allNotes.size());
        verify(notesRepository, times(1)).findAll();
    }
}
