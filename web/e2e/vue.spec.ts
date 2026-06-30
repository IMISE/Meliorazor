import { test, expect } from '@playwright/test'

// See here how to get started:
// https://playwright.dev/docs/intro
test("visits the app root url and see nothing", async ({ page }) => {
  await page.goto("/")
  await expect(page.getByText("Meliorazor")).toBeVisible();
  await expect(page.getByText("Bitte loggen Sie sich ein")).toBeVisible();
  await expect(page.getByText("Meine Projekte")).toBeHidden();
})
