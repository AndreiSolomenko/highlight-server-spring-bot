from flask import Flask, request, jsonify
from paddleocr import PaddleOCR
import cv2
import numpy as np

app = Flask(__name__)
ocr = PaddleOCR(use_angle_cls=True, lang='en')  # lang='uk' не підтримується, лише en, ch, fr, de, etc.

@app.route("/ocr", methods=["POST"])
def ocr_image():
    file = request.files['image']
    img = cv2.imdecode(np.frombuffer(file.read(), np.uint8), cv2.IMREAD_COLOR)
    result = ocr.ocr(img, cls=True)

    text = "\n".join([line[1][0] for block in result for line in block])
    return jsonify({"text": text})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5050)
