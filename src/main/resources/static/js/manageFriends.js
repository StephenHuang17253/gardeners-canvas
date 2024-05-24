document.addEventListener('DOMContentLoaded', function () {
    const tabs = document.querySelectorAll('.tab-links a');
    const tabContents = document.querySelectorAll('.tab');

    tabs.forEach(tab => {
        tab.addEventListener('click', function(e) {
            e.preventDefault();
            const target = this.getAttribute('href').substring(1);
            tabs.forEach(tab => tab.parentElement.classList.remove('active'));
            tabContents.forEach(tabContent => tabContent.classList.remove('active'));

            this.parentElement.classList.add('active');
            document.getElementById(target).classList.add('active');
        })
    })
})