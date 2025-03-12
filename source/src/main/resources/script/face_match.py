import sys
import json
import face_recognition
import numpy as np
import cv2
import os

# Define the path where Spring Boot serves static files
STATIC_FOLDER = "src/main/resources/static/"
SAVE_FILENAME = "id_card_face.png"

def ensure_static_directory():
    """
    Ensure the static folder exists before saving the file.
    """
    if not os.path.exists(STATIC_FOLDER):
        os.makedirs(STATIC_FOLDER)

def load_and_detect_face(image_path, save_face=False):
    """
    Load an image, detect the face, return the face encoding, and optionally save the detected face.
    """
    image = face_recognition.load_image_file(image_path)
    face_locations = face_recognition.face_locations(image)

    if len(face_locations) == 0:
        return None, "No face detected"

    face_encodings = face_recognition.face_encodings(image, face_locations)

    if len(face_encodings) == 0:
        return None, "Face detected but encoding failed"

    # Extract face region and save it as an image if requested
    if save_face:
        ensure_static_directory()  # Ensure static directory exists
        save_path = os.path.join(STATIC_FOLDER, SAVE_FILENAME)

        top, right, bottom, left = face_locations[0]  # Get first detected face
        face_image = image[top:bottom, left:right]  # Crop the face
        face_bgr = cv2.cvtColor(face_image, cv2.COLOR_RGB2BGR)  # Convert RGB to BGR for OpenCV

        cv2.imwrite(save_path, face_bgr)  # Save the face image

    return face_encodings[0], "Face detected successfully"

def compare_faces(image_path_1, image_path_2, save_id_face=True):
    """
    Compare the faces from two image paths. Saves the detected face from the ID card image to Spring Boot's static folder.
    """
    encoding1, msg1 = load_and_detect_face(image_path_1)
    encoding2, msg2 = load_and_detect_face(image_path_2, save_face=save_id_face)

    if encoding1 is None:
        return {"match": False, "message": f"Error with image 1: {msg1}"}
    if encoding2 is None:
        return {"match": False, "message": f"Error with image 2: {msg2}"}

    # Compare faces using Euclidean distance
    distance = np.linalg.norm(encoding1 - encoding2)
    threshold = 0.6  # Lower means stricter match

    is_match = distance < threshold

    return {
        "match": bool(is_match),
        "distance": float(round(distance, 4)),
        "threshold": float(threshold),
    }

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print(json.dumps({"error": "Usage: python face_match.py <image1> <image2>"}))
        sys.exit(1)

    image1_path = sys.argv[1]
    image2_path = sys.argv[2]

    result = compare_faces(image1_path, image2_path)

    print("RESULT")
    print(f"Match: {result['match']}")
    print(f"Distance: {result['distance']}")
    print(f"Threshold: {result['threshold']}")
