function handleFileSelect(event) {
    const files = event.target.files;
    if (files.length > 0) {
        const file = files[0];
        const reader = new FileReader();

        reader.onload = (e) => {
            const previewImage = document.getElementById('imageToChange');
            previewImage.src = e.target.result;
        };
        reader.readAsDataURL(file);
    }
}