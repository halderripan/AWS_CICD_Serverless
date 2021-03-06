package com.csye6225.noteapp.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.csye6225.noteapp.model.NoteEntity;
import com.csye6225.noteapp.model.NoteEntityWrapper;
import com.csye6225.noteapp.model.UserEntity;
import com.csye6225.noteapp.service.NoteService;
import com.csye6225.noteapp.service.UserService;

@RestController
public class NoteController {

	@Autowired
	NoteService noteService;

	@Autowired
	UserService userService;

	@GetMapping("/note")
	public List<NoteEntityWrapper> getAllNotes() {
		// check the user if authenticated
		List<NoteEntity> noteList = noteService.findAll(userService.currentUserId().getId());
		List<NoteEntityWrapper> noteEntityList = new ArrayList<>();
		for (NoteEntity note : noteList) {
			NoteEntityWrapper noteWrapper = new NoteEntityWrapper(note);
			noteWrapper.setAttachments(noteService.getListAttachments(note.getId()));
			noteEntityList.add(noteWrapper);
		}
		return noteEntityList;
	}

	@PostMapping("/note")
	public ResponseEntity<Object> createNote(@Valid @RequestBody NoteEntity note) {
		HashMap<String, Object> entities = new HashMap<String, Object>();
		note.setUserId(userService.currentUserId().getId());
		NoteEntityWrapper noteWrapper = new NoteEntityWrapper(noteService.createNote(note));
		noteWrapper.setAttachments(noteService.getListAttachments(noteWrapper.getNote_id()));
		entities.put("note", noteWrapper);
		return ResponseEntity.ok(entities);
	}

	@GetMapping("/note/{id}")
	public ResponseEntity<Object> getNoteById(@PathVariable(value = "id") String noteId) {
		HashMap<String, Object> entities = new HashMap<String, Object>();
		NoteEntity note = noteService.getNoteById(noteId);
		if (null == note) {
			entities.put("message", "Note does not exists");
			return new ResponseEntity<>(entities, HttpStatus.NOT_FOUND);
		} else if (userService.currentUserId().getId() == note.getUserId()) {
			NoteEntityWrapper noteWrapper = new NoteEntityWrapper(noteService.getNoteById(noteId));
			noteWrapper.setAttachments(noteService.getListAttachments(noteId));
			entities.put("note", noteWrapper);
			return ResponseEntity.ok(entities);

		} else {
			entities.put("message", "Not authorized to read this note");
			return new ResponseEntity<>(entities, HttpStatus.UNAUTHORIZED);
		}
	}

	@PutMapping("/note/{id}")
	public ResponseEntity<Object> updateNote(@PathVariable(value = "id") String noteId,
			@Valid @RequestBody NoteEntity noteDetails) {
		// Check if user authenticated and authorized
		NoteEntity note = noteService.getNoteById(noteId);
		HashMap<String, Object> entities = new HashMap<String, Object>();
		if (null == note) {
			entities.put("message", "Note does not exists");
			return new ResponseEntity<>(entities, HttpStatus.BAD_REQUEST);
		} else if (userService.currentUserId().getId() == note.getUserId()) {
			note.setTitle(noteDetails.getTitle());
			note.setContent(noteDetails.getContent());
			note.setUpdatedAt(new Date());

			NoteEntityWrapper noteWrapper = new NoteEntityWrapper(noteService.updateNote(note));
			noteWrapper.setAttachments(noteService.getListAttachments(noteId));

			entities.put("note", noteWrapper);
			return ResponseEntity.ok(entities);

		} else {
			entities.put("message", "Not authorized to update this note");
			return new ResponseEntity<>(entities, HttpStatus.UNAUTHORIZED);
		}

	}

	@DeleteMapping("/note/{id}")
	public ResponseEntity<?> deleteNote(@PathVariable(value = "id") String noteId) {
		NoteEntity note = noteService.getNoteById(noteId);

		HashMap<String, Object> entities = new HashMap<String, Object>();
		if (null == note) {
			entities.put("message", "Note does not exists");
			return new ResponseEntity<>(entities, HttpStatus.BAD_REQUEST);
		} else if (userService.currentUserId().getId() == note.getUserId()) {
			noteService.deleteNote(note);

			entities.put("Deleted", "Note was successfuly deleted");
			return new ResponseEntity<>(entities, HttpStatus.NO_CONTENT);

		} else {
			entities.put("message", "Not authorized to Delete this note");
			return new ResponseEntity<>(entities, HttpStatus.UNAUTHORIZED);
		}

	}

}
