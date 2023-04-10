import { initialize, exlog, extract } from './extractor';
import isInterface from './util/is-interface';
import memory from './memory';
import { normalize } from './file-processor';

const output = document.querySelector(`#output`);
const fileselect = document.querySelector(`#mcfolder`);
const filedrop = document.querySelector(`#filezone`);
const startbutton = document.querySelector(`#extract`);
const versionlist = document.querySelector(`#version-list`);
const snapshots = document.querySelector(`#snapshotversions`);
const historical = document.querySelector(`#historicalversions`);

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
      ghbuild = new Date(num * 1000).toUTCString().trim();

      console.log(`got build date:`, ghbuild);
    } else {
      // get rev hash
      ghrev = text.trim();
      console.log(`got revision hash:`, ghrev);
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

async function fetchVersions(snapshots: boolean, historical: boolean) {
  versionlist.innerHTML = ``;
  fetch(`https://piston-meta.mojang.com/mc/game/version_manifest_v2.json`).then(async (result) => {
    const json = await result.json();
    json.versions.forEach((version: Object) => {
      if (snapshots === false && /[0-9]+w|pre|rc|3D/gi.test(version.id)) return;
      if (historical === false && /rd|c0.|a1.|b1.|inf/gi.test(version.id)) return;

      let option = document.createElement("li");
      option.tabIndex = 0;
      option.innerHTML = version.id;
      versionlist?.append(option);
    });
  })
}

fetchVersions(false, false);

/**
   * force reset file selection and extract buttons on page load to prevent weirdness
   * 
   * i probably could've just used autocomplete="off" on these two elements,
   * but i've heard that doesn't work right on some browsers.
   * 
   * fun!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   */
if (fileselect && startbutton) {
  if (isInterface<HTMLInputElement>(fileselect, `files`)) {
    fileselect.value = ``
  }

  if (isInterface<HTMLInputElement>(startbutton, `disabled`)) {
    startbutton.disabled = true;
  }
}

fileselect?.addEventListener(`input`, () => {
  console.log(`input`)
});

fileselect?.addEventListener(`change`, () => {
  if (!isInterface<HTMLInputElement>(fileselect, `files`)) return;

  const files = fileselect?.files;

  if (files == null) return;

  normalize(files);

  if (!isInterface<HTMLInputElement>(startbutton, `disabled`)) return;
  startbutton.disabled = false;
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

  const files = e.dataTransfer?.items;

  if (files == null) return;

  normalize(files);

  if (!isInterface<HTMLInputElement>(startbutton, `disabled`)) return;
  startbutton.disabled = false;
}, false);

startbutton?.addEventListener(`click`, () => {
  if (memory.files.length > 0) {
    extract();
  } else {
    // TODO: it'd probably be better to disable the button altogether in this case
    alert(`No files selected!`);
  }
});

snapshots?.addEventListener(`change`, () => {
  fetchVersions(snapshots?.checked, historical?.checked)
});

historical?.addEventListener(`change`, () => {
  fetchVersions(snapshots?.checked, historical?.checked)
});