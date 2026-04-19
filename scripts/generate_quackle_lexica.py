#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import struct
from dataclasses import dataclass, field
from pathlib import Path
from typing import Iterable


QUACKLE_NULL_MARK = 0
QUACKLE_BLANK_MARK = 1
QUACKLE_PLAYED_THRU_MARK = 2
QUACKLE_PLAYTHRU_START_MARK = 3
QUACKLE_PLAYTHRU_END_MARK = 4
QUACKLE_FIRST_LETTER = 5
QUACKLE_MAXIMUM_ALPHABET_SIZE = 55
QUACKLE_BLANK_OFFSET = QUACKLE_MAXIMUM_ALPHABET_SIZE

INTERNAL_SEPARATOR_REPRESENTATION = QUACKLE_FIRST_LETTER + QUACKLE_MAXIMUM_ALPHABET_SIZE


@dataclass(frozen=True)
class AlphabetEntry:
    text: str
    blank_text: str
    score: int
    count: int
    is_vowel: bool
    code: int


@dataclass
class AlphabetSpec:
    letters: list[AlphabetEntry]

    @property
    def encoding(self) -> dict[str, int]:
        return {entry.text.upper(): entry.code for entry in self.letters}


def parse_alphabet_file(path: Path) -> AlphabetSpec:
    letters: list[AlphabetEntry] = []
    next_code = QUACKLE_FIRST_LETTER
    with path.open("r", encoding="utf-8") as handle:
        for raw_line in handle:
            line = raw_line.strip()
            if not line or line.startswith("#"):
                continue
            parts = line.split()
            if parts[0].lower() == "blank":
                continue
            if len(parts) < 5:
                raise ValueError(f"Invalid alphabet line: {raw_line.rstrip()}")
            letters.append(
                AlphabetEntry(
                    text=parts[0],
                    blank_text=parts[1],
                    score=int(parts[2]),
                    count=int(parts[3]),
                    is_vowel=parts[4] == "1",
                    code=next_code,
                )
            )
            next_code += 1
    return AlphabetSpec(letters)


def read_word_list(path: Path) -> list[str]:
    seen: set[str] = set()
    words: list[str] = []
    with path.open("r", encoding="utf-8") as handle:
        for raw_line in handle:
            word = raw_line.strip().upper()
            if not word:
                continue
            if word in seen:
                continue
            seen.add(word)
            words.append(word)
    words.sort()
    return words


def encode_word(word: str, alphabet: AlphabetSpec) -> bytes:
    mapping = alphabet.encoding
    try:
        return bytes(mapping[char] for char in word)
    except KeyError as exc:
        raise ValueError(f"Unencodable character {exc.args[0]!r} in word {word!r}") from exc


def xor_md5_hash(words: Iterable[bytes]) -> bytes:
    acc = [0, 0, 0, 0]
    for word in words:
        digest = hashlib.md5(word).digest()
        parts = struct.unpack("<4I", digest)
        for index, value in enumerate(parts):
            acc[index] ^= value
    return struct.pack("<4I", *acc)


@dataclass
class DawgNode:
    c: int
    insmallerdict: bool = False
    playability: int = 0
    children: list["DawgNode"] = field(default_factory=list)
    pointer: int = 0
    location: int = 0
    lastchild: bool = False
    sumexplored: bool = False
    sum: int = 0
    deleted: bool = False
    cloneof: "DawgNode | None" = None
    written: bool = False

    def push_word(self, word: bytes, in_smaller: bool, playability: int) -> bool:
        if not word:
            added = self.playability == 0
            self.playability = 1 if playability == 0 else playability
            self.insmallerdict = in_smaller
        else:
            first = word[0]
            rest = word[1:]
            index = -1
            for child_index, child in enumerate(self.children):
                if child.c == first:
                    index = child_index
                    break
            if index == -1:
                self.children.append(DawgNode(c=first))
                index = len(self.children) - 1
            added = self.children[index].push_word(rest, in_smaller, playability)
        self.sumexplored = False
        self.deleted = False
        self.written = False
        return added

    def equals(self, other: "DawgNode") -> bool:
        if self.playability != other.playability:
            return False
        if self.c != other.c:
            return False
        if len(self.children) != len(other.children):
            return False
        if self.insmallerdict != other.insmallerdict:
            return False
        if self.sum != other.sum:
            return False
        for left, right in zip(self.children, other.children):
            if not left.equals(right):
                return False
        return True

    def letter_sum(self) -> int:
        if self.sumexplored:
            return self.sum
        self.sumexplored = True
        value = ((5381 * 33) + int(self.c)) & 0xFFFFFFFF
        for child in self.children:
            value = ((value << 5) + value + child.letter_sum()) & 0xFFFFFFFF
        self.sum = value
        return self.sum

    def print_nodes(self, node_list: list["DawgNode"]) -> None:
        self.written = True
        if not self.children:
            return
        if not self.deleted:
            self.pointer = len(node_list)
        else:
            assert self.cloneof is not None
            self.pointer = self.cloneof.pointer
        if not self.deleted:
            for child in self.children:
                node_list.append(child)
            for child in self.children:
                if not child.deleted:
                    child.print_nodes(node_list)
                elif child.cloneof is not None and not child.cloneof.written:
                    child.cloneof.print_nodes(node_list)
        self.children[-1].lastchild = True


def write_dawg(words: list[bytes], alphabet: AlphabetSpec, output_path: Path) -> tuple[int, bytes]:
    root = DawgNode(c=QUACKLE_BLANK_MARK)
    encodable = 0
    for word in words:
        if root.push_word(word, True, 0):
            encodable += 1

    node_list: list[DawgNode] = [root]
    root.print_nodes(node_list)
    root.letter_sum()

    buckets: list[list[int]] = [[] for _ in range(2000)]
    for index, node in enumerate(node_list):
        buckets[node.sum % 2000].append(index)
        node.pointer = 0
        node.written = False
        node.deleted = False
        node.cloneof = None

    for bucket in buckets:
        if not bucket:
            continue
        for left_offset, left_index in enumerate(bucket[:-1]):
            left = node_list[left_index]
            if left.deleted:
                continue
            for right_index in bucket[left_offset + 1 :]:
                right = node_list[right_index]
                if right.deleted:
                    continue
                if left.equals(right):
                    right.deleted = True
                    right.cloneof = left

    node_list = [root]
    root.print_nodes(node_list)

    hash_bytes = xor_md5_hash(words)
    with output_path.open("wb") as handle:
        handle.write(b"\x01")
        handle.write(hash_bytes)
        handle.write(bytes(((encodable >> 16) & 0xFF, (encodable >> 8) & 0xFF, encodable & 0xFF)))
        handle.write(bytes((len(alphabet.letters),)))
        for letter in alphabet.letters:
            handle.write(letter.text.encode("utf-8"))
            handle.write(b" ")
        for node in node_list:
            pointer = node.cloneof.pointer if node.deleted and node.cloneof is not None else node.pointer
            byte3 = node.c - QUACKLE_FIRST_LETTER
            if node.lastchild:
                byte3 |= 64
            if node.insmallerdict:
                byte3 |= 128
            playability = node.playability
            handle.write(
                bytes(
                    (
                        (pointer >> 16) & 0xFF,
                        (pointer >> 8) & 0xFF,
                        pointer & 0xFF,
                        byte3 & 0xFF,
                        (playability >> 16) & 0xFF,
                        (playability >> 8) & 0xFF,
                        playability & 0xFF,
                    )
                )
            )
    return encodable, hash_bytes


@dataclass
class GaddagNode:
    c: int
    t: bool = False
    children: list["GaddagNode"] = field(default_factory=list)
    pointer: int = 0
    lastchild: bool = False

    def push_word(self, word: bytes) -> None:
        if not word:
            self.t = True
            return
        first = word[0]
        rest = word[1:]
        index = -1
        for child_index, child in enumerate(self.children):
            if child.c == first:
                index = child_index
                break
        if index == -1:
            self.children.append(GaddagNode(c=first))
            index = len(self.children) - 1
        self.children[index].push_word(rest)

    def print_nodes(self, node_list: list["GaddagNode"]) -> None:
        if self.children:
            self.pointer = len(node_list)
            self.children[-1].lastchild = True
        for child in self.children:
            node_list.append(child)
        for child in self.children:
            child.print_nodes(node_list)


def gaddagize(word: bytes) -> list[bytes]:
    transformed: list[bytes] = []
    for index in range(1, len(word) + 1):
        prefix = bytes(reversed(word[:index]))
        if index < len(word):
            transformed.append(prefix + bytes((INTERNAL_SEPARATOR_REPRESENTATION,)) + word[index:])
        else:
            transformed.append(prefix)
    return transformed


def write_gaddag(words: list[bytes], output_path: Path, hash_bytes: bytes) -> int:
    transformed_words: list[bytes] = []
    for word in words:
        transformed_words.extend(gaddagize(word))
    transformed_words.sort()

    root = GaddagNode(c=QUACKLE_NULL_MARK)
    for word in transformed_words:
        root.push_word(word)

    node_list: list[GaddagNode] = [root]
    root.print_nodes(node_list)

    with output_path.open("wb") as handle:
        handle.write(b"\x01")
        handle.write(hash_bytes)
        for index, node in enumerate(node_list):
            pointer = node.pointer
            if pointer != 0:
                pointer -= index
            letter = QUACKLE_NULL_MARK if node.c == INTERNAL_SEPARATOR_REPRESENTATION else node.c
            byte3 = letter & 0x3F
            if node.t:
                byte3 |= 64
            if node.lastchild:
                byte3 |= 128
            handle.write(
                bytes(
                    (
                        (pointer >> 16) & 0xFF,
                        (pointer >> 8) & 0xFF,
                        pointer & 0xFF,
                        byte3 & 0xFF,
                    )
                )
            )
    return len(node_list)


def generate_lexicon(
    word_list_path: Path,
    alphabet_path: Path,
    dawg_output_path: Path,
    gaddag_output_path: Path,
) -> None:
    alphabet = parse_alphabet_file(alphabet_path)
    raw_words = read_word_list(word_list_path)
    encoded_words = [encode_word(word, alphabet) for word in raw_words]
    dawg_count, hash_bytes = write_dawg(encoded_words, alphabet, dawg_output_path)
    gaddag_nodes = write_gaddag(encoded_words, gaddag_output_path, hash_bytes)
    print(
        f"{word_list_path.name}: words={len(encoded_words)} "
        f"dawg={dawg_output_path.name} gaddag={gaddag_output_path.name} "
        f"hash={hash_bytes.hex()} gaddag_nodes={gaddag_nodes} encodable={dawg_count}"
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate Quackle DAWG/GADDAG files from plain word lists.")
    parser.add_argument(
        "--project-root",
        default=Path(__file__).resolve().parents[1],
        type=Path,
        help="Project root that contains src/resources and quackle-master.",
    )
    args = parser.parse_args()

    project_root = args.project_root.resolve()
    alphabet_path = project_root / "quackle-master" / "data" / "alphabets" / "english.quackle_alphabet"
    lexica_dir = project_root / "quackle-master" / "data" / "lexica"
    lexica_dir.mkdir(parents=True, exist_ok=True)

    generate_lexicon(
        project_root / "src" / "resources" / "Dicts" / "North-America" / "NWL2018.txt",
        alphabet_path,
        lexica_dir / "nwl18.dawg",
        lexica_dir / "nwl18.gaddag",
    )
    generate_lexicon(
        project_root / "src" / "resources" / "Dicts" / "British" / "CSW19.txt",
        alphabet_path,
        lexica_dir / "csw19.dawg",
        lexica_dir / "csw19.gaddag",
    )


if __name__ == "__main__":
    main()
