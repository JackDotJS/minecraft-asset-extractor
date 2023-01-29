import { initialize, exlog } from './extractor'
import isInterface from './util/is-interface'

const output = document.querySelector(`#output`);
const fileselect = document.querySelector(`#mcfolder`);
const filedrop = document.querySelector(`#filezone`);

let ghbuild = "";
let ghrev = "";

initialize(output);

Promise.all([
  fetch(`gha-build.txt`),
  fetch(`gha-hash.txt`)
]).then(async (results) => {
  for (const i in results) {
    const request = results[i];

    if (request.status !== 200) console.error(request.status);

    const text = await request.text();

    if (i === `0`) {
      // get build date
      const num = parseInt(text);
      if (isNaN(num)) return console.warn(`gha-build: ${num}`);
      ghbuild = new Date(num * 1000).toUTCString();

      console.log(`got build date:`, ghbuild);
    } else {
      // get rev hash
      ghrev = text;
      console.log(`got revision hash:`, text);
    }
  }

  exlog([
    `=== Debug Information ===`,
    `Revision Hash: ${ghrev}`,
    `Build Date: ${ghbuild}`,
    ``,
    `UserAgent: ${navigator.userAgent}`
  ].join(`\n`));
}).catch(console.warn);


fileselect?.addEventListener(`input`, (e) => {
  console.log(`input`)
});

fileselect?.addEventListener(`change`, (e) => {
  if (!isInterface<HTMLInputElement>(fileselect, `files`)) return;

  const files = fileselect?.files;

  if (files == null) return;

  exlog(`Loaded ${files.length} files.`);
});

filedrop?.addEventListener(`dragenter`, (e) => {
  e.preventDefault();
  e.stopPropagation();
  filedrop?.classList.add(`dragover`);
});

filedrop?.addEventListener(`dragover`, (e) => {
  e.preventDefault();
  e.stopPropagation();
  filedrop?.classList.add(`dragover`);
});

filedrop?.addEventListener(`dragleave`, (e) => {
  e.preventDefault();
  e.stopPropagation();
  filedrop?.classList.remove(`dragover`);
});

filedrop?.addEventListener(`drop`, (e) => {
  e.preventDefault();
  e.stopPropagation();
  filedrop?.classList.remove(`dragover`);

  if (!isInterface<DragEvent>(e, `dataTransfer`)) return;

  console.log(e.dataTransfer)

  if (e.dataTransfer?.items) {
    console.log(e.dataTransfer.items);
  }
}, false);