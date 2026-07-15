import { mount } from "@vue/test-utils";
import { describe, it, expect } from "vitest";
import { createVuetify } from "vuetify";
import * as components from "vuetify/components";
import * as directives from "vuetify/directives";

import AnnotationBadge from "../AnnotationBadge.vue";
import { Status } from "@/types/enums.js";
import type { MultiAuthorAnnotation } from "@/types/types.js";

const vuetify = createVuetify({
  components,
  directives,
});

const singleAuthorAnnotation: MultiAuthorAnnotation = {
  id: 0,
  authorNames: ["TestUser"],
  authorNameToId: {
    TestUser: 0,
  },
  code: {
    system: "http://example.com/codes",
    systemVersion: "http://example.com/codes/12345",
    code: "XYZ",
    display: "X, Y and Z",
  },
  status: Status.ACCEPTED,
  comments: [],
  confidence: 100,
  comment: "",
  predicate: "owl:sameAs",
};

describe("AnnotationBadge", () => {
  it("shows correct text", () => {
    const wrapper = mount(AnnotationBadge, {
      props: {
        annotation: singleAuthorAnnotation,
        projectId: 0,
        projectName: "",
        dataElementId: 0,
        dataElementName: "",
        allowEditSelection: false,
        dashedBorder: false,
      },
      global: {
        plugins: [vuetify],
      },
    });

    expect(wrapper.text()).toContain("owl:sameAs");
    expect(wrapper.text()).toContain("X, Y and Z");
  });
});
