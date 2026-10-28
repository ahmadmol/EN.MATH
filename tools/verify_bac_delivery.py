"""Compatibility entry point: validate the complete current G9/BAC delivery."""
import runpy
from pathlib import Path

runpy.run_path(str(Path(__file__).with_name("verify_unified_delivery.py")), run_name="__main__")
